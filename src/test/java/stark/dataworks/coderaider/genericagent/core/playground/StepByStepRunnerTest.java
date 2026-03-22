package stark.dataworks.coderaider.genericagent.core.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.agent.AgentDefinition;
import stark.dataworks.coderaider.genericagent.core.agent.AgentRegistry;
import stark.dataworks.coderaider.genericagent.core.context.ContextResult;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.events.RunEvent;
import stark.dataworks.coderaider.genericagent.core.events.RunEventType;
import stark.dataworks.coderaider.genericagent.core.examples.ExampleSupport;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunner;
import stark.dataworks.coderaider.genericagent.core.runner.RunConfiguration;
import stark.dataworks.coderaider.genericagent.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.genericagent.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.LocalShellTool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 33) Dynamic multi-agent debug workflow inspired by Trae, Cursor, Antigravity.
 * <p>
 * Agent architecture:
 * - Planner: Understands problem first, reads files, decides output format based on complexity
 * - Executor: Executes plan, tracks progress, can request plan adjustment
 * - Summarizer: Reports what was done
 * <p>
 * Key design principles:
 * - Generic agents: no hardcoded file paths in system prompts
 * - Dynamic planning: simple problems get solution ideas, complex problems get step-by-step plans
 * - Progress tracking: Executor updates and reports progress
 * - Manual orchestration: Java code controls the flow, not automatic handoff
 */
public class StepByStepRunnerTest
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String MODEL = "Qwen/Qwen3.5-27B";
    private static final Path INPUT_FILE_1 = Path.of("src", "test", "resources", "inputs", "FinancialCalculator.py");
    private static final Path INPUT_FILE_2 = Path.of("src", "test", "resources", "inputs", "OrderProcessor.py");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(20, null, 0.0, 1024, "auto", "text", Map.of());

    /**
     * Control whether to truncate tool call arguments and results in output.
     * Set to -1 for no truncation, or a positive number for max characters.
     */
    private static final int TRUNCATE_OUTPUT_LENGTH = -1;

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
        agentRegistry.register(createPlannerAgent(runtimeOs, workspace));
        agentRegistry.register(createExecutorAgent(runtimeOs, workspace));
        agentRegistry.register(createSummarizerAgent(runtimeOs, workspace));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createStreamingPublisher())
            .build();

        String userRequest = """
            Fix the bugs in the Python files.
            
            Working directory: %s
            
            Files to fix:
            - FinancialCalculator.py
            - OrderProcessor.py
            
            IMPORTANT:
            - For local_shell (type, python): use FULL PATH like "%s\\FinancialCalculator.py"
            - For apply_patch: use FILENAME ONLY like "FinancialCalculator.py"
            
            Verification:
            - Run: cd /d "%s" ; python OrderProcessor.py
            - Expected: output should contain BEHAVIOR_OK
            """.formatted(workspace.toString(), workspace, workspace);

        System.out.println("\n========== [PLANNER PHASE] ==========\n");

        ContextResult planning = runner.chatClient("react30-planner").prompt().stream(true).user(userRequest)
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();

        String verifyOutput = runBehaviorVerification(runtimeOs, workspace);
        ContextResult execution = null;
        String currentPlan = planning.getFinalOutput();
        int maxIterations = 10;
        int iteration = 0;

        while (iteration < maxIterations)
        {
            iteration++;
            String sourceSnapshot1 = Files.readString(workspace.resolve("FinancialCalculator.py"));
            String sourceSnapshot2 = Files.readString(workspace.resolve("OrderProcessor.py"));
            System.out.println("\n========== [EXECUTOR PHASE - Iteration " + iteration + "] ==========\n");

            execution = runner.chatClient("react30-executor").prompt().stream(true)
                .user(buildExecutorPrompt(currentPlan, verifyOutput, iteration, workspace, sourceSnapshot1, sourceSnapshot2))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();

            verifyOutput = runBehaviorVerification(runtimeOs, workspace);
            System.out.println("\n[VERIFICATION - Iteration " + iteration + "]:\n" + verifyOutput.trim());

            if (verifyOutput.contains("BEHAVIOR_OK"))
            {
                System.out.println("\n[SUCCESS] Bugs fixed successfully!");
                break;
            }

            if (execution.getFinalOutput().contains("NEED_PLAN_ADJUSTMENT"))
            {
                System.out.println("\n========== [PLANNER RE-PLAN] ==========\n");
                ContextResult newPlanning = runner.chatClient("react30-planner").prompt().stream(true)
                    .user(buildPlanAdjustmentPrompt(currentPlan, execution.getFinalOutput(), verifyOutput))
                    .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();
                currentPlan = newPlanning.getFinalOutput();
            }
        }

        System.out.println("\n========== [SUMMARIZER PHASE] ==========\n");

        ContextResult summary = runner.chatClient("react30-summarizer").prompt().stream(true)
            .user(buildSummarizerPrompt(currentPlan, execution.getFinalOutput(), verifyOutput))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION).runHooks(ExampleSupport.noopHooks()).call().contextResult();

        Assertions.assertTrue(verifyOutput.contains("BEHAVIOR_OK"),
            "Agent must fix the bugs successfully. Verification output: " + verifyOutput);
        Assertions.assertNotNull(planning.getFinalOutput());
        Assertions.assertNotNull(execution.getFinalOutput());

        String summaryText = summary.getFinalOutput();
        Assertions.assertFalse(summaryText.isBlank());
        Assertions.assertTrue(summaryText.contains("Files") || summaryText.contains("Changes") || summaryText.contains("Outcome"),
            "Expected summary with relevant sections. Got: " + summaryText);

        long elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000L;
        Assertions.assertTrue(elapsedSeconds <= 150, "Expected runtime (<=150s) but took " + elapsedSeconds + "s");
    }

    private static RunEventPublisher createStreamingPublisher()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new ReActTraceListener());
        return publisher;
    }

    private static String buildExecutorPrompt(String plan, String verifyOutput, int iteration, Path workspace, String sourceSnapshot1, String sourceSnapshot2)
    {
        return """
            Attempt %d to fix the bugs.

            === CRITICAL INSTRUCTIONS ===
            The planner has already identified the bugs. Your job is to EXECUTE the fixes, not re-analyze.
            
            1. Read the planner's findings below carefully.
            2. Apply ALL fixes using apply_patch tool.
            3. Each patch must contain the EXACT lines to change.
            4. After patching, run the verifier immediately.
            
            === PLANNER FINDINGS ===
            %s

            === CURRENT VERIFICATION STATUS ===
            %s

            === FinancialCalculator.py ===
            %s

            === OrderProcessor.py ===
            %s

            === EXECUTION STEPS ===
            1. Apply patches for ALL bugs (use apply_patch tool with path="FinancialCalculator.py" or "OrderProcessor.py")
            2. Run verifier: cd /d "%s" ; python OrderProcessor.py
            3. If output shows BEHAVIOR_OK, stop.
            """.formatted(iteration, plan, verifyOutput.trim(), sourceSnapshot1, sourceSnapshot2, workspace);
    }

    private static String buildPlanAdjustmentPrompt(String currentPlan, String executorFeedback, String verifyOutput)
    {
        return """
            The executor requested a plan adjustment.
            
            === CURRENT PLAN ===
            %s
            
            === EXECUTOR FEEDBACK ===
            %s
            
            === VERIFICATION RESULT ===
            %s
            
            Please adjust the plan based on this feedback.
            Keep the new plan concise and focused on the remaining issues.
            """.formatted(currentPlan, executorFeedback, verifyOutput.trim());
    }

    private static String buildSummarizerPrompt(String plan, String execution, String verifyOutput)
    {
        return """
            Summarize what was done during this debugging session.
            
            === PLANNING OUTPUT ===
            %s
            
            === EXECUTION OUTPUT ===
            %s
            
            === VERIFICATION RESULT ===
            %s
            
            === YOUR SUMMARY ===
            Provide a concise summary with:
            - Files modified: [list files]
            - Changes made: [brief description of changes]
            - Outcome: [success/failure and final result]
            """.formatted(plan, execution, verifyOutput.trim());
    }

    private static AgentDefinition createPlannerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-planner");
        def.setName("Planner");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a code bug investigator.

            Inspect source and verifier output to identify root causes.
            Read the file(s) by the local_shell tool before your investigation.
            Report exact bug locations and expected behavior.

            OS: %s
            Workspace: %s
            Verify command: %s
            
            For fixing bugs, handoff to 'react30-executor', with your investigation results.
            To handoff, respond with 'handoff: <agent_id>' where agent_id is 'react30-executor'.
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.verifyCommand(workspace)));
        def.setReactInstructions("Read file + verifier output, then return a concrete root-cause list for each failing behavior.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setHandoffAgentIds(List.of("react30-executor"));
        return def;
    }

    private static AgentDefinition createExecutorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-executor");
        def.setName("Executor");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a code fixer.

            Rules:
            - Fix bugs in FinancialCalculator.py and OrderProcessor.py
            - Run verification after patching
            - Stop when verification output contains BEHAVIOR_OK
            - Follow Python syntax when generating code

            OS: %s
            Workspace: %s
            Verify command: %s
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.verifyCommand(workspace)));
        def.setReactInstructions("Read -> patch -> verify. Keep fixing until BEHAVIOR_OK.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        return def;
    }

    private static AgentDefinition createSummarizerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react30-summarizer");
        def.setName("Summarizer");
        def.setModel(MODEL);
        def.setReactEnabled(false);
        def.setSystemPrompt("""
            You are a task summarizer. Summarize what was done.
            
            Your summary should include:
            1. What files were modified
            2. What changes were made
            3. What the outcome was
            
            Keep it concise and informative.
            
            OS: %s
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("""
            ## Summary
            - Files modified: [list files]
            - Changes made: [brief description of changes]
            - Outcome: [success/failure and final result]
            """);
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
        Files.writeString(workspace.resolve("FinancialCalculator.py"), Files.readString(INPUT_FILE_1));
        Files.writeString(workspace.resolve("OrderProcessor.py"), Files.readString(INPUT_FILE_2));
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
                "cd /d \"" + workspace + "\" && python OrderProcessor.py");
            case MACOS, LINUX -> new ProcessBuilder("bash", "-lc",
                "cd '" + workspace + "' && python OrderProcessor.py");
        };
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), runtimeConsoleCharset());
            process.waitFor();
            return output;
        }
        catch (Exception ex)
        {
            return "VERIFY_ERROR: " + ex.getMessage();
        }
    }

    private static Charset runtimeConsoleCharset()
    {
        String nativeEncoding = System.getProperty("native.encoding");
        if (nativeEncoding != null && !nativeEncoding.isBlank())
        {
            try
            {
                return Charset.forName(nativeEncoding);
            }
            catch (Exception ignored)
            {
            }
        }
        return Charset.defaultCharset();
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
            return "python OrderProcessor.py";
        }

        private String printFileCommand(String fileName)
        {
            return switch (this)
            {
                case WINDOWS -> "type " + fileName;
                case MACOS, LINUX -> "cat " + fileName;
            };
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

    private static final class ReActTraceListener implements IRunEventListener
    {
        private String currentAgent;
        private boolean thoughtHeaderPrinted;
        private boolean actionHeaderPrinted;
        private boolean observationHeaderPrinted;
        private boolean answerHeaderPrinted;

        @Override
        public void onEvent(RunEvent event)
        {
            String agent = (String) event.getAttributes().get("agent");
            if (agent != null)
            {
                currentAgent = agent;
            }

            if (event.getType() == RunEventType.MODEL_REASONING_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!thoughtHeaderPrinted)
                    {
                        System.out.println("\n[" + agentPrefix() + "Thought]");
                        thoughtHeaderPrinted = true;
                        actionHeaderPrinted = false;
                        observationHeaderPrinted = false;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
                return;
            }

            if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object args = event.getAttributes().get("arguments");
                if (!actionHeaderPrinted)
                {
                    System.out.println("\n\n[" + agentPrefix() + "Action]");
                    actionHeaderPrinted = true;
                }
                System.out.println("tool=" + tool + " args=" + formatValue(args));
                observationHeaderPrinted = false;
                return;
            }

            if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object result = event.getAttributes().get("result");
                if (!observationHeaderPrinted)
                {
                    System.out.println("[" + agentPrefix() + "Observation]");
                    observationHeaderPrinted = true;
                }
                System.out.println("tool=" + tool + " result=" + formatValue(result));
                thoughtHeaderPrinted = false;
                return;
            }

            if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!answerHeaderPrinted)
                    {
                        System.out.println("\n\n[" + agentPrefix() + "Answer]");
                        answerHeaderPrinted = true;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
            }
        }

        private String agentPrefix()
        {
            return currentAgent != null ? currentAgent + " " : "";
        }

        private String formatValue(Object value)
        {
            if (value == null)
            {
                return "null";
            }
            try
            {
                String json;
                if (value instanceof String str)
                {
                    Object normalized = OBJECT_MAPPER.readValue(str, Object.class);
                    json = OBJECT_MAPPER.writeValueAsString(normalized);
                }
                else
                {
                    json = OBJECT_MAPPER.writeValueAsString(value);
                }

                if (TRUNCATE_OUTPUT_LENGTH < 0)
                {
                    return json;
                }

                String normalized = json.replace("\r", " ").replace("\n", " ").trim();
                if (normalized.length() <= TRUNCATE_OUTPUT_LENGTH)
                {
                    return normalized;
                }
                return normalized.substring(0, TRUNCATE_OUTPUT_LENGTH) + "...(truncated)";
            }
            catch (Exception e)
            {
                return String.valueOf(value);
            }
        }
    }
}
