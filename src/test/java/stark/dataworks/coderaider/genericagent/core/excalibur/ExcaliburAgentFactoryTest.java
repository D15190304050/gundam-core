package stark.dataworks.coderaider.genericagent.core.excalibur;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ExcaliburAgentFactoryTest
{
    @Test
    void createBuildsWorkspaceAwareSoftwareEngineeringPrompt()
    {
        Path workspace = Path.of("src", "test", "resources", "outputs", "excalibur-factory-test");

        AgentDefinition definition = ExcaliburAgentFactory.create(
            "excalibur-investigator",
            "Qwen/Qwen3-4B",
            workspace,
            ExcaliburAgentRole.INVESTIGATOR,
            "Inspect first, then explain the root cause.",
            "Verify command: python verifier.py");

        Assertions.assertEquals("excalibur-investigator", definition.getId());
        Assertions.assertEquals(ExcaliburAgentRole.INVESTIGATOR.getDefaultName(), definition.getName());
        Assertions.assertTrue(definition.isReactEnabled());
        Assertions.assertEquals(workspace.toAbsolutePath().normalize().toString(),
            definition.getModelProviderOptions().get("working_directory"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("You are an expert AI software engineering agent."));
        Assertions.assertTrue(definition.getSystemPrompt().contains("Task-specific instructions:"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("GUIDE FOR HOW TO USE THE `sequentialthinking` TOOL:"));
        Assertions.assertTrue(definition.getSystemPrompt().contains("Verify command: python verifier.py"));
        Assertions.assertTrue(definition.getReactInstructions().contains("Inspect first"));
        Assertions.assertEquals(ExcaliburAgentRole.INVESTIGATOR.getDefaultToolNames(), definition.getToolNames());
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

            ExcaliburAgentSpec spec = ExcaliburAgentFactory.createSpec(
                "excalibur-fixer",
                "Excalibur Fixer",
                "Qwen/Qwen3-4B",
                workspace,
                ExcaliburAgentRole.FIXER,
                request,
                "Patch and verify.",
                "Verify command: python demo.py",
                ExcaliburAgentRole.FIXER.getDefaultToolNames(),
                java.util.List.of(),
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
        run(workspace, "git", "init");
        run(workspace, "git", "config", "user.email", "test@example.com");
        run(workspace, "git", "config", "user.name", "Test User");
        run(workspace, "git", "add", ".");
        run(workspace, "git", "commit", "-m", "initial");
    }

    private static String resolveHeadCommit(Path workspace) throws IOException
    {
        return run(workspace, "git", "rev-parse", "HEAD").trim();
    }

    private static String run(Path workspace, String... command) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workspace.toFile());
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                throw new IOException(String.join(" ", command) + " failed: " + output);
            }
            return output;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running command", ex);
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
                    throw new RuntimeException(ex);
                }
            });
    }
}
