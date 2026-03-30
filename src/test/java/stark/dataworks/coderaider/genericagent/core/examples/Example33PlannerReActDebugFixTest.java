package stark.dataworks.coderaider.genericagent.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;
import stark.dataworks.coderaider.genericagent.core.agent.AgentRegistry;
import stark.dataworks.coderaider.genericagent.core.context.ContextResult;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunner;
import stark.dataworks.coderaider.genericagent.core.runner.RunConfiguration;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.LocalShellTool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 33) Single agent debug workflow for Python files.
 */
public class Example33PlannerReActDebugFixTest
{
    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final Path INPUT_FILE_1 = Path.of("src", "test", "resources", "inputs", "FinancialCalculator.py");
    private static final Path INPUT_FILE_2 = Path.of("src", "test", "resources", "inputs", "OrderProcessor.py");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(15, null, 0.0, 1024, "auto", "text", Map.of());

    @Test
    public void run() throws IOException
    {
        long startedAt = System.nanoTime();
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.out.println("Skipping test: MODEL_SCOPE_API_KEY not set");
            return;
        }

        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example33").toAbsolutePath();
        Path targetFile1 = resetWorkspace(workspace);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(createFixerAgent(workspace));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle("Fixer "))
            .build();

        String initialVerify = runBehaviorVerification(workspace);
        System.out.println("INITIAL_VERIFICATION: " + initialVerify.trim());

        ContextResult fixerResult = null;
        String verifyOutput = initialVerify;
        
        for (int attempt = 1; attempt <= 5; attempt++)
        {
            String sourceSnapshot1 = Files.readString(workspace.resolve("FinancialCalculator.py"));
            String sourceSnapshot2 = Files.readString(workspace.resolve("OrderProcessor.py"));
            
            fixerResult = runner.chatClient("react33-fixer")
                .prompt()
                .stream(true)
                .user(buildFixerPrompt(workspace, attempt, verifyOutput, sourceSnapshot1, sourceSnapshot2))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            System.out.println("ATTEMPT_" + attempt + "_OUTPUT: " + fixerResult.getFinalOutput());

            verifyOutput = runBehaviorVerification(workspace);
            System.out.println("ATTEMPT_" + attempt + "_VERIFICATION: " + verifyOutput.trim());
            
            if (verifyOutput.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        Assertions.assertNotNull(fixerResult, "Expected fixer output");
        Assertions.assertTrue(verifyOutput.contains("BEHAVIOR_OK"),
            "Agent must fix the bugs successfully. Verification output: " + verifyOutput);

        long elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000L;
        Assertions.assertTrue(elapsedSeconds <= 300, "Expected runtime (<=300s) but took " + elapsedSeconds + "s");
    }

    private static AgentDefinition createFixerAgent(Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react33-fixer");
        def.setName("Python Bug Fixer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a Python code fixer. Debug and fix bugs in Python files.
            
            Process:
            1. Read the buggy files to understand the code
            2. Run the code to see the current (buggy) output
            3. Identify the bugs by analyzing the code logic
            4. Apply patches to fix the bugs
            5. Verify the fix by running the code again
            
            Workspace: %s
            
            Important: Use apply_patch tool to fix files. The patch format is:
            --- file.py
            +++ file.py
            @@ -line,count +line,count @@
            -old line
            +new line
            """.formatted(workspace));
        def.setReactInstructions("Read files → Run to see error → Find bugs → Apply patches → Verify → Done.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "medium"));
        return def;
    }

    private static String buildFixerPrompt(Path workspace, int attempt, String verifyOutput, String source1, String source2)
    {
        return """
            Attempt %d to fix the Python bugs.
            
            Files to fix:
            - FinancialCalculator.py
            - OrderProcessor.py
            
            Current verification output:
            %s
            
            Expected: Running "python OrderProcessor.py" should output "BEHAVIOR_OK total=1958.34"
            
            Current FinancialCalculator.py (first 100 lines):
            %s
            
            Current OrderProcessor.py (first 100 lines):
            %s
            
            Steps:
            1. Read both files completely using local_shell (cat or type command)
            2. Run "python OrderProcessor.py" to see current output
            3. Analyze the code to find bugs
            4. Apply patches to fix the bugs
            5. Verify by running "python OrderProcessor.py" again
            
            Workspace: %s
            """.formatted(attempt, verifyOutput.trim(), 
                truncate(source1, 100), truncate(source2, 100), 
                workspace);
    }

    private static String truncate(String text, int maxLines)
    {
        String[] lines = text.split("\n", -1);
        if (lines.length <= maxLines)
        {
            return text;
        }
        return String.join("\n", java.util.Arrays.copyOf(lines, maxLines)) + "\n... (truncated)";
    }

    private static LocalShellTool createShellTool()
    {
        ToolDefinition definition = new ToolDefinition(
            "local_shell",
            "Execute a local shell command and return stdout/stderr.",
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")));
        return new LocalShellTool(definition);
    }

    private static Path resetWorkspace(Path workspace) throws IOException
    {
        if (Files.exists(workspace))
        {
            Files.walk(workspace)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(path ->
                {
                    try
                    {
                        Files.delete(path);
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                });
        }
        Files.createDirectories(workspace);
        Path targetFile1 = workspace.resolve("FinancialCalculator.py");
        Path targetFile2 = workspace.resolve("OrderProcessor.py");
        Files.writeString(targetFile1, Files.readString(INPUT_FILE_1));
        Files.writeString(targetFile2, Files.readString(INPUT_FILE_2));
        return targetFile1;
    }

    private static String runBehaviorVerification(Path workspace)
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder("python", "OrderProcessor.py");
            builder.directory(workspace.toFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor();
            return output;
        }
        catch (Exception ex)
        {
            return "VERIFY_ERROR: " + ex.getMessage();
        }
    }

    private static final class FileSystemEditor implements IApplyPatchEditor
    {
        private final Path workspaceRoot;

        private FileSystemEditor(Path workspaceRoot)
        {
            this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
        }

        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            return upsert(operation, true);
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            return upsert(operation, false);
        }

        @Override
        public ApplyPatchResult deleteFile(ApplyPatchOperation operation)
        {
            if (operation == null || operation.getPath() == null)
            {
                return ApplyPatchResult.failed("Invalid operation");
            }
            Path target = workspaceRoot.resolve(operation.getPath()).normalize();
            if (!target.startsWith(workspaceRoot))
            {
                return ApplyPatchResult.failed("Path escapes workspace");
            }
            try
            {
                Files.deleteIfExists(target);
                return ApplyPatchResult.completed("Deleted " + operation.getPath());
            }
            catch (IOException ex)
            {
                return ApplyPatchResult.failed("Delete failed: " + ex.getMessage());
            }
        }

        private ApplyPatchResult upsert(ApplyPatchOperation operation, boolean createMode)
        {
            if (operation == null || operation.getPath() == null)
            {
                return ApplyPatchResult.failed("Invalid operation");
            }
            Path target = workspaceRoot.resolve(operation.getPath()).normalize();
            if (!target.startsWith(workspaceRoot))
            {
                return ApplyPatchResult.failed("Path escapes workspace");
            }
            try
            {
                Files.createDirectories(target.getParent());
                if (createMode)
                {
                    String content = ApplyPatchTool.applyCreateDiff(operation.getDiff());
                    Files.writeString(target, content);
                    return ApplyPatchResult.completed("Created " + operation.getPath());
                }
                String source = Files.exists(target) ? Files.readString(target) : "";
                String patched = ApplyPatchTool.applyDiff(source, operation.getDiff());

                if (patched.equals(source))
                {
                    return ApplyPatchResult.failed(buildDiffNotMatchError(operation.getDiff()));
                }

                Files.writeString(target, patched);
                return ApplyPatchResult.completed("Updated " + operation.getPath());
            }
            catch (Exception ex)
            {
                return ApplyPatchResult.failed("Patch failed: " + ex.getMessage());
            }
        }

        private static String buildDiffNotMatchError(String diff)
        {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Diff failed: content not found in file. No changes were applied.\n\n");
            errorMsg.append("The diff you provided:\n");
            if (diff != null)
            {
                String[] lines = diff.split("\\R", -1);
                int shown = 0;
                for (String line : lines)
                {
                    if (shown >= 5) break;
                    if (line.startsWith("-") || line.startsWith("+"))
                    {
                        String display = line.length() > 100 ? line.substring(0, 100) + "..." : line;
                        errorMsg.append("  ").append(display).append("\n");
                        shown++;
                    }
                }
            }
            errorMsg.append("\nTo fix this:\n");
            errorMsg.append("1. Read the file again to get its CURRENT content.\n");
            errorMsg.append("2. Compare your diff with the actual file content.\n");
            errorMsg.append("3. Provide a new diff that matches EXACTLY what's in the file (including whitespace).\n");
            return errorMsg.toString();
        }
    }
}
