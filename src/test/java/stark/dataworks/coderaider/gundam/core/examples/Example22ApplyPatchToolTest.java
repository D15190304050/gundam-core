package stark.dataworks.coderaider.gundam.core.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.gundam.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ApplyPatchTool;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Example demonstrating ApplyPatchTool for file diff/patch operations.
 * The agent can create, update, and delete files using unified diffs.
 */
public class Example22ApplyPatchToolTest
{
    @Test
    public void run() throws IOException
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));

        if (apiKey == null || apiKey.isBlank())
        {
            System.out.println("Skipping test: MODEL_SCOPE_API_KEY not set");
            return;
        }

        String model = "Qwen/Qwen3-4B";

        Path tempDir = Files.createTempDirectory("apply-patch-example-");
        InMemoryEditor editor = new InMemoryEditor(tempDir);
        ApplyPatchTool patchTool = new ApplyPatchTool(editor, false);

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("patch-agent");
        agentDef.setName("Patch Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a file editing assistant. Use the apply_patch tool to create and modify files. " +
            "The workspace is at: " + tempDir.toString());
        agentDef.setToolNames(List.of("apply_patch"));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(agentDef);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(patchTool);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle(""))
            .build();

        System.out.println("=== ApplyPatchTool Example ===");
        System.out.println("Workspace: " + tempDir);
        System.out.println("Running agent to create a file...\n");

        ContextResult result = runner.chatClient("patch-agent")
            .prompt()
            .user("Create a file called 'tasks.md' with a simple TODO list containing 3 items.")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("\nFinal output: " + result.getFinalOutput());

        System.out.println("\n=== Files Created ===");
        editor.printFiles();

        System.out.println("\n=== DiffApplier Test ===");
        String original = "line1\nline2\nline3";
        String diff = " line1\n-line2\n+line2_modified\n line3";
        String patched = ApplyPatchTool.applyDiff(original, diff);
        System.out.println("Original: " + original);
        System.out.println("Diff: " + diff);
        System.out.println("Patched: " + patched);

        System.out.println("\nExample completed successfully!");

        cleanup(tempDir);
    }

    private void cleanup(Path dir)
    {
        try
        {
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try
                    {
                        Files.deleteIfExists(p);
                    }
                    catch (IOException e)
                    {
                    }
                });
        }
        catch (IOException e)
        {
        }
    }

    private static class InMemoryEditor implements IApplyPatchEditor
    {
        private final Path root;
        private final Map<String, String> files = new java.util.concurrent.ConcurrentHashMap<>();

        public InMemoryEditor(Path root)
        {
            this.root = root;
        }

        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            String path = operation.getPath();
            String diff = operation.getDiff();
            String content = ApplyPatchTool.applyCreateDiff(diff);
            files.put(path, content);

            try
            {
                Path filePath = root.resolve(path);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, content);
            }
            catch (IOException e)
            {
                return ApplyPatchResult.failed("Failed to create file: " + e.getMessage());
            }

            return ApplyPatchResult.completed("Created " + path);
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            String path = operation.getPath();
            String diff = operation.getDiff();
            String original = files.getOrDefault(path, "");

            try
            {
                Path filePath = root.resolve(path);
                if (Files.exists(filePath))
                {
                    original = Files.readString(filePath);
                }

                String patched = ApplyPatchTool.applyDiff(original, diff);
                files.put(path, patched);
                Files.writeString(filePath, patched);

                return ApplyPatchResult.completed("Updated " + path);
            }
            catch (IOException e)
            {
                return ApplyPatchResult.failed("Failed to update file: " + e.getMessage());
            }
        }

        @Override
        public ApplyPatchResult deleteFile(ApplyPatchOperation operation)
        {
            String path = operation.getPath();
            files.remove(path);

            try
            {
                Path filePath = root.resolve(path);
                Files.deleteIfExists(filePath);
                return ApplyPatchResult.completed("Deleted " + path);
            }
            catch (IOException e)
            {
                return ApplyPatchResult.failed("Failed to delete file: " + e.getMessage());
            }
        }

        public void printFiles()
        {
            files.forEach((path, content) -> {
                System.out.println("\n--- " + path + " ---");
                System.out.println(content);
            });
        }
    }
}
