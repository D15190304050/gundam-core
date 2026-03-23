package stark.dataworks.coderaider.genericagent.core.excalibur;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.excalibur.tools.ExcaliburToolRegistrySupport;
import stark.dataworks.coderaider.genericagent.core.tool.ITool;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class ExcaliburTraeCompatibilityTest
{
    @Test
    void rolesExposeTraeCompatibleToolSets()
    {
        Assertions.assertEquals(ExcaliburTraeToolNames.ALL, ExcaliburAgentRole.INVESTIGATOR.getDefaultToolNames());
        Assertions.assertTrue(ExcaliburAgentRole.FIXER.getDefaultToolNames().containsAll(ExcaliburTraeToolNames.ALL));
        Assertions.assertTrue(ExcaliburAgentRole.FIXER.getDefaultToolNames().contains("apply_patch"));
        Assertions.assertEquals(ExcaliburTraeToolNames.ALL, ExcaliburAgentRole.REVIEWER.getDefaultToolNames());
        Assertions.assertEquals(ExcaliburTraeToolNames.ALL, ExcaliburAgentRole.SUMMARIZER.getDefaultToolNames());
    }


    @Test
    void bashToolKeepsSharedSessionAndSupportsRestart() throws IOException
    {
        Path workspace = Files.createTempDirectory("excalibur-bash-tools");
        try
        {
            ToolRegistry registry = new ToolRegistry();
            ExcaliburToolRegistrySupport.registerTraeCompatibleTools(registry, workspace, new NoOpEditor());
            ITool bashTool = registry.get(ExcaliburTraeToolNames.BASH).orElseThrow();

            String first = bashTool.execute(Map.of("command", "mkdir -p nested && cd nested && pwd"));
            Assertions.assertTrue(first.contains(workspace.resolve("nested").toAbsolutePath().normalize().toString()));

            String second = bashTool.execute(Map.of("command", "pwd"));
            Assertions.assertTrue(second.contains(workspace.resolve("nested").toAbsolutePath().normalize().toString()));

            String restarted = bashTool.execute(Map.of("command", "pwd", "restart", true));
            Assertions.assertEquals("tool has been restarted.", restarted);

            String afterRestart = bashTool.execute(Map.of("command", "pwd"));
            Assertions.assertTrue(afterRestart.contains(workspace.toAbsolutePath().normalize().toString()));
        }
        finally
        {
            deleteRecursively(workspace);
        }
    }

    @Test
    void toolRegistrySupportRegistersTraeCompatibleToolsAndEditToolWorks() throws IOException
    {
        Path workspace = Files.createTempDirectory("excalibur-trae-tools");
        try
        {
            Path sample = workspace.resolve("sample.txt");
            Files.writeString(sample, "alpha\nbeta\n");

            ToolRegistry registry = new ToolRegistry();
            ExcaliburToolRegistrySupport.registerTraeCompatibleTools(registry, workspace, new NoOpEditor());

            ITool editTool = registry.get(ExcaliburTraeToolNames.STR_REPLACE_BASED_EDIT_TOOL).orElseThrow();
            String viewed = editTool.execute(Map.of(
                "command", "view",
                "path", sample.toAbsolutePath().toString()));
            Assertions.assertTrue(viewed.contains("alpha"));

            String replaced = editTool.execute(Map.of(
                "command", "str_replace",
                "path", sample.toAbsolutePath().toString(),
                "old_str", "beta",
                "new_str", "gamma"));
            Assertions.assertTrue(replaced.contains("has been edited"));
            Assertions.assertEquals("alpha\ngamma\n", Files.readString(sample));

            ITool bashTool = registry.get(ExcaliburTraeToolNames.BASH).orElseThrow();
            String bashOutput = bashTool.execute(Map.of("command", "pwd"));
            Assertions.assertTrue(bashOutput.contains(workspace.toAbsolutePath().normalize().toString()));

            ITool doneTool = registry.get(ExcaliburTraeToolNames.TASK_DONE).orElseThrow();
            Assertions.assertEquals("Task done.", doneTool.execute(Map.of()));
        }
        finally
        {
            deleteRecursively(workspace);
        }
    }

    @Test
    void jsonEditToolSupportsSimpleTraeStyleMutations() throws IOException
    {
        Path workspace = Files.createTempDirectory("excalibur-json-tools");
        try
        {
            Path sample = workspace.resolve("sample.json");
            Files.writeString(sample, "{\"user\":{\"name\":\"Ada\"}}\n");

            ToolRegistry registry = new ToolRegistry();
            ExcaliburToolRegistrySupport.registerTraeCompatibleTools(registry, workspace, new NoOpEditor());

            ITool jsonTool = registry.get(ExcaliburTraeToolNames.JSON_EDIT_TOOL).orElseThrow();
            String setResult = jsonTool.execute(Map.of(
                "operation", "add",
                "file_path", sample.toAbsolutePath().toString(),
                "json_path", "$.user.role",
                "value", "engineer"));
            Assertions.assertTrue(setResult.contains("Successfully added value at JSONPath"));

            String viewResult = jsonTool.execute(Map.of(
                "operation", "view",
                "file_path", sample.toAbsolutePath().toString(),
                "json_path", "$.user"));
            Assertions.assertTrue(viewResult.contains("engineer"));

            String removeResult = jsonTool.execute(Map.of(
                "operation", "remove",
                "file_path", sample.toAbsolutePath().toString(),
                "json_path", "$.user.role"));
            Assertions.assertTrue(removeResult.contains("Successfully removed value(s) at JSONPath"));
            Assertions.assertFalse(Files.readString(sample).contains("engineer"));
        }
        finally
        {
            deleteRecursively(workspace);
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

    private static final class NoOpEditor implements IApplyPatchEditor
    {
        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            return ApplyPatchResult.completed("noop");
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            return ApplyPatchResult.completed("noop");
        }

        @Override
        public ApplyPatchResult deleteFile(ApplyPatchOperation operation)
        {
            return ApplyPatchResult.completed("noop");
        }
    }
}
