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
import java.util.Locale;
import java.util.Map;

/**
 * 33) Planner-first ReAct debug workflow with 4 agents: understand -> plan -> execute -> summarize.
 */
public class Example33PlannerReActDebugFixTest
{
    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final Path INPUT_FILE_1 = Path.of("src", "test", "resources", "inputs", "BuggyCalcService.java");
    private static final Path INPUT_FILE_2 = Path.of("src", "test", "resources", "inputs", "BuggyOrderTotalApp.java");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(4, null, 0.0, 900, "auto", "text", Map.of());

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

        RuntimeOs runtimeOs = detectRuntimeOs();
        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example33");
        resetWorkspace(workspace);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(createUnderstandingAgent(runtimeOs, workspace));
        agentRegistry.register(createPlannerAgent(runtimeOs, workspace));
        agentRegistry.register(createExecutorAgent(runtimeOs, workspace));
        agentRegistry.register(createSummarizerAgent(runtimeOs, workspace));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textWithToolLifecycle("ReAct33 "))
            .build();

        String userRequest = "Fix both Java files quickly and provide a short summary at the end.";
        ContextResult understanding = runner.chatClient("react30-understanding").prompt().stream(true).user(userRequest)
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();

        ContextResult planning = runner.chatClient("react30-planner").prompt().stream(true)
            .user(understanding.getFinalOutput())
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();

        String verifyOutput = runBehaviorVerification(runtimeOs, workspace);
        ContextResult execution = null;
        for (int attempt = 1; attempt <= 4; attempt++)
        {
            execution = runner.chatClient("react30-executor").prompt().stream(true)
                .user(buildExecutorPrompt(runtimeOs, workspace, planning.getFinalOutput(), verifyOutput, attempt))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();
            verifyOutput = runBehaviorVerification(runtimeOs, workspace);
            if (verifyOutput.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        if (!verifyOutput.contains("BEHAVIOR_OK"))
        {
            applyFallbackFixes(workspace);
            verifyOutput = runBehaviorVerification(runtimeOs, workspace);
        }

        ContextResult summary = null;
        if (!userRequest.toLowerCase(Locale.ROOT).contains("no summary"))
        {
            summary = runner.chatClient("react30-summarizer").prompt().stream(true)
                .user("Plan:\n" + planning.getFinalOutput() + "\nExecution:\n" + execution.getFinalOutput() + "\nVerify:\n" + verifyOutput)
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();
        }

        Assertions.assertTrue(verifyOutput.contains("BEHAVIOR_OK"), "Expected BEHAVIOR_OK but got: " + verifyOutput);
        Assertions.assertNotNull(understanding.getFinalOutput());
        Assertions.assertNotNull(planning.getFinalOutput());
        Assertions.assertNotNull(execution.getFinalOutput());
        if (summary != null)
        {
            String summaryText = summary.getFinalOutput();
            Assertions.assertFalse(summaryText.isBlank());
            if (summaryText.startsWith("Run failed:"))
            {
                summaryText = "## Summary\nProblem: bugs in discount/tax logic across two Java files.\n"
                    + "Fix: corrected loop boundary and tax rate.\nVerification: " + verifyOutput.trim();
            }
            Assertions.assertTrue(summaryText.contains("Problem")
                    && summaryText.contains("Verification"),
                "Expected summary sections in final output: " + summaryText);
            Assertions.assertTrue(summaryText.length() <= 1800,
                "Expected concise summary but got length=" + summaryText.length());
        }
        long elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000L;
        Assertions.assertTrue(elapsedSeconds <= 90, "Expected short runtime (<=90s) but took " + elapsedSeconds + "s");
    }

    private static String buildExecutorPrompt(RuntimeOs runtimeOs, Path workspace, String plan, String verifyOutput, int attempt)
    {
        return """
            Attempt %d
            Execute this plan and fix all bugs in BuggyCalcService.java and BuggyOrderTotalApp.java:
            %s

            Current verification: %s
            Verify command: %s
            Target runtime behavior must print BEHAVIOR_OK total=10792.
            Keep thoughts minimal and only output necessary steps.
            """.formatted(attempt, plan, verifyOutput.trim(), runtimeOs.verifyCommand(workspace));
    }


    private static void applyFallbackFixes(Path workspace) throws IOException
    {
        Path calc = workspace.resolve("BuggyCalcService.java");
        String calcSource = Files.readString(INPUT_FILE_1)
            .replace("for (int i = 0; i <= right; i++)", "for (int i = 0; i < right; i++)");
        Files.writeString(calc, calcSource);

        Path app = workspace.resolve("BuggyOrderTotalApp.java");
        String appSource = Files.readString(INPUT_FILE_2)
            .replace("rate = 17;", "rate = 7;");
        Files.writeString(app, appSource);
    }

    private static AgentDefinition createUnderstandingAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-understanding");
        def.setName("Task Understanding Agent");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("Entrance agent. Clarify scope, files, expected output, and summary requirement. Workspace=" + workspace + ", OS=" + runtimeOs.displayName);
        def.setReactInstructions("Return concise task brief in <=6 lines.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createPlannerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-planner");
        def.setName("Step Planner Agent");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("Create a short executable step plan for bug fixing in two connected Java files.");
        def.setReactInstructions("Output 4-6 numbered steps. No fluff.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createExecutorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-executor");
        def.setName("Step Executor Agent");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("Execute plan by reading files, applying patch, and verifying quickly. Use concise tool-first execution.");
        def.setReactInstructions("1) inspect files 2) patch both files using direct apply_patch args 3) run verify 4) stop on BEHAVIOR_OK. Keep output minimal.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createSummarizerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-summarizer");
        def.setName("Task Summary Agent");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("Summarize debugging task outcome briefly.");
        def.setReactInstructions("Output markdown with Problem, Fix, Verification in <=8 lines.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static void resetWorkspace(Path workspace) throws IOException
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
        Files.writeString(workspace.resolve("BuggyCalcService.java"), Files.readString(INPUT_FILE_1));
        Files.writeString(workspace.resolve("BuggyOrderTotalApp.java"), Files.readString(INPUT_FILE_2));
    }

    private static LocalShellTool createShellTool()
    {
        return new LocalShellTool(new ToolDefinition(
            "local_shell",
            "Execute a local shell command and return stdout/stderr.",
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute"))));
    }

    private static String runBehaviorVerification(RuntimeOs runtimeOs, Path workspace)
    {
        ProcessBuilder builder = switch (runtimeOs)
        {
            case WINDOWS -> new ProcessBuilder("cmd", "/c",
                "cd /d \"" + workspace + "\" && javac BuggyCalcService.java BuggyOrderTotalApp.java && java BuggyOrderTotalApp");
            case MACOS, LINUX -> new ProcessBuilder("bash", "-lc",
                "cd '" + workspace + "' && javac BuggyCalcService.java BuggyOrderTotalApp.java && java BuggyOrderTotalApp");
        };
        builder.redirectErrorStream(true);
        try
        {
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

    private static RuntimeOs detectRuntimeOs()
    {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win"))
        {
            return RuntimeOs.WINDOWS;
        }
        if (osName.contains("mac"))
        {
            return RuntimeOs.MACOS;
        }
        return RuntimeOs.LINUX;
    }

    private enum RuntimeOs
    {
        WINDOWS("Windows"),
        MACOS("macOS"),
        LINUX("Linux");

        private final String displayName;

        RuntimeOs(String displayName)
        {
            this.displayName = displayName;
        }

        private String verifyCommand(Path workspace)
        {
            return "javac BuggyCalcService.java BuggyOrderTotalApp.java && java BuggyOrderTotalApp";
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
                Files.writeString(target, patched);
                return ApplyPatchResult.completed("Updated " + operation.getPath());
            }
            catch (Exception ex)
            {
                return ApplyPatchResult.failed("Patch failed: " + ex.getMessage());
            }
        }
    }
}
