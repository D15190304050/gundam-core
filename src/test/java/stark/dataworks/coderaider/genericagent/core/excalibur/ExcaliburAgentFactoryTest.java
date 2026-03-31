package stark.dataworks.coderaider.genericagent.core.excalibur;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class ExcaliburAgentFactoryTest
{
    @Test
    void createBuildsWorkspaceAwareSoftwareEngineeringPrompt()
    {
        Path workspace = Path.of("src", "test", "resources", "outputs", "excalibur-factory-test");
        List<String> toolNames = ExcaliburTraeToolNames.ALL;

        AgentDefinition definition = ExcaliburAgentFactory.create(
            "excalibur-investigator",
            "Excalibur Investigator",
            "Qwen/Qwen3-4B",
            workspace,
            "Inspect first, then explain the root cause.",
            "Verify command: python verifier.py",
            toolNames);

        Assertions.assertEquals("excalibur-investigator", definition.getId());
        Assertions.assertEquals("Excalibur Investigator", definition.getName());
        Assertions.assertTrue(definition.isReactEnabled());
        Assertions.assertEquals(workspace.toAbsolutePath().normalize().toString(),
            definition.getModelProviderOptions().get("working_directory"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("You are an expert AI software engineering agent."));
        Assertions.assertTrue(definition.getSystemPrompt().contains("Task-specific instructions:"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("GUIDE FOR HOW TO USE THE `sequentialthinking` TOOL:"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("Verify command: python verifier.py"));
        Assertions.assertTrue(definition.getReactInstructions().contains("Inspect first"));
        Assertions.assertEquals(toolNames, definition.getToolNames());
    }

    @Test
    void createSpecBuildsTraeStyleBootstrapMessageAndPatchContract() throws IOException
    {
        Path workspace = Files.createTempDirectory("excalibur-spec-test");
        try
        {
            Files.writeString(workspace.resolve("demo.py"), "print('demo')\n");
            initializeGitRepository(workspace);
            String baseCommit = ExcaliburPatchUtils.getGitDiff(workspace, null).isBlank()
                ? resolveHeadCommit(workspace)
                : "";
            ExcaliburTaskRequest request = ExcaliburTaskRequest.builder("Fix demo.py and leave a patch.", workspace)
                .issue("demo.py is wrong")
                .baseCommit(baseCommit)
                .mustPatch(true)
                .patchPath(workspace.resolve("demo.patch"))
                .build();

            List<String> toolNames = List.of(
                ExcaliburTraeToolNames.STR_REPLACE_BASED_EDIT_TOOL,
                ExcaliburTraeToolNames.JSON_EDIT_TOOL,
                ExcaliburTraeToolNames.BASH,
                ExcaliburTraeToolNames.SEQUENTIAL_THINKING,
                ExcaliburTraeToolNames.TASK_DONE,
                "apply_patch");

            ExcaliburAgentSpec spec = ExcaliburAgentFactory.createSpec(
                "excalibur-fixer",
                "Excalibur Fixer",
                "Qwen/Qwen3-4B",
                workspace,
                request,
                "Patch and verify.",
                "Verify command: python demo.py",
                toolNames,
                List.of(),
                true,
                "medium");

            String initialMessage = spec.initialUserMessage();
            Assertions.assertTrue(initialMessage.contains("[Project root path]:"));
            Assertions.assertTrue(initialMessage.contains("[Problem statement]:"));
            Assertions.assertTrue(initialMessage.contains("[Patch requirement]:"));
            Assertions.assertFalse(spec.hasRequiredPatch());
            Assertions.assertTrue(Files.exists(workspace.resolve("demo.patch")));
            Assertions.assertEquals(ExcaliburPatchUtils.taskIncompleteMessage(), spec.taskIncompleteMessage());
        }
        finally
        {
            deleteRecursively(workspace);
        }
    }

    @Test
    void hasRequiredPatchDetectsWorkingTreeChangesAgainstBaseCommit() throws IOException
    {
        Path workspace = Files.createTempDirectory("excalibur-patch-test");
        try
        {
            Path sourceFile = workspace.resolve("demo.py");
            Files.writeString(sourceFile, "print('before')\n");
            initializeGitRepository(workspace);
            String baseCommit = resolveHeadCommit(workspace);
            Files.writeString(sourceFile, "print('after')\n");

            ExcaliburTaskRequest request = ExcaliburTaskRequest.builder("Fix demo.py", workspace)
                .baseCommit(baseCommit)
                .mustPatch(true)
                .patchPath(workspace.resolve("demo.patch"))
                .build();

            Assertions.assertTrue(ExcaliburPatchUtils.hasRequiredPatch(request));
            String diff = Files.readString(workspace.resolve("demo.patch"));
            Assertions.assertTrue(diff.contains("print('after')"));
            Assertions.assertFalse(diff.contains("diff --git a/src/test"));
        }
        finally
        {
            deleteRecursively(workspace);
        }
    }

    private static void initializeGitRepository(Path workspace) throws IOException
    {
        try
        {
            Process process = new ProcessBuilder("git", "init").directory(workspace.toFile()).start();
            process.waitFor();
            Process addProcess = new ProcessBuilder("git", "add", "-A").directory(workspace.toFile()).start();
            addProcess.waitFor();
            Process commitProcess = new ProcessBuilder("git", "commit", "-m", "initial")
                .directory(workspace.toFile())
                .start();
            commitProcess.waitFor();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Git initialization interrupted", e);
        }
    }

    private static String resolveHeadCommit(Path workspace)
    {
        try
        {
            Process process = new ProcessBuilder("git", "rev-parse", "HEAD")
                .directory(workspace.toFile())
                .start();
            process.waitFor();
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream())))
            {
                return reader.readLine();
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return "";
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private static void deleteRecursively(Path root) throws IOException
    {
        if (root == null || !Files.exists(root))
        {
            return;
        }
        Files.walk(root)
            .sorted((left, right) -> right.getNameCount() - left.getNameCount())
            .forEach(path ->
            {
                try
                {
                    Files.deleteIfExists(path);
                }
                catch (IOException ex)
                {
                    // ignore
                }
            });
    }
}