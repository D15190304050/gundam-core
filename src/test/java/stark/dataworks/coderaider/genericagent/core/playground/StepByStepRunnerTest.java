package stark.dataworks.coderaider.genericagent.core.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.context.ContextResult;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.events.RunEvent;
import stark.dataworks.coderaider.genericagent.core.events.RunEventType;
import stark.dataworks.coderaider.genericagent.core.examples.ExampleSupport;
import stark.dataworks.coderaider.genericagent.core.excalibur.ExcaliburAgentFactory;
import stark.dataworks.coderaider.genericagent.core.excalibur.ExcaliburAgentRole;
import stark.dataworks.coderaider.genericagent.core.excalibur.ExcaliburAgentSpec;
import stark.dataworks.coderaider.genericagent.core.excalibur.ExcaliburTaskRequest;
import stark.dataworks.coderaider.genericagent.core.excalibur.tools.ExcaliburToolRegistrySupport;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunner;
import stark.dataworks.coderaider.genericagent.core.runner.RunConfiguration;
import stark.dataworks.coderaider.genericagent.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.genericagent.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.genericagent.core.agent.AgentRegistry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dynamic Excalibur-driven multi-agent debug workflow for general-purpose software engineering tasks.
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
        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "excalibur-step-by-step");
        resetWorkspace(workspace);
        ExcaliburTaskRequest taskRequest = createTaskRequest(workspace);

        ToolRegistry toolRegistry = new ToolRegistry();
        ExcaliburToolRegistrySupport.registerTraeCompatibleTools(toolRegistry, workspace, new FileSystemEditor(workspace));

        ExcaliburAgentSpec investigatorSpec = createInvestigatorAgent(runtimeOs, workspace, taskRequest);
        ExcaliburAgentSpec fixerSpec = createFixerAgent(runtimeOs, workspace, taskRequest);
        ExcaliburAgentSpec reviewerSpec = createReviewerAgent(runtimeOs, workspace, taskRequest);
        ExcaliburAgentSpec summarizerSpec = createSummarizerAgent(workspace, taskRequest);

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(investigatorSpec.toAgentDefinition());
        agentRegistry.register(fixerSpec.toAgentDefinition());
        agentRegistry.register(reviewerSpec.toAgentDefinition());
        agentRegistry.register(summarizerSpec.toAgentDefinition());

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL, false))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(createStreamingPublisher())
            .build();

        String behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
        System.out.println("INITIAL_VERIFICATION: " + behaviorOutput.trim());

        ContextResult investigation = runner.chatClient(investigatorSpec.toAgentDefinition().getId())
            .prompt()
            .stream(true)
            .user(investigatorSpec.initialUserMessage() + "\n\n" + buildInvestigatorPrompt(runtimeOs, workspace, behaviorOutput))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("INVESTIGATION_OUTPUT: " + investigation.getFinalOutput());
        abortIfProviderUnavailable("investigator", investigation.getFinalOutput());

        ContextResult fixerResult = null;
        ContextResult reviewerResult = null;
        for (int attempt = 1; attempt <= 5; attempt++)
        {
            String financialSnapshot = Files.readString(workspace.resolve("FinancialCalculator.py"));
            String orderSnapshot = Files.readString(workspace.resolve("OrderProcessor.py"));

            fixerResult = runner.chatClient(fixerSpec.toAgentDefinition().getId())
                .prompt()
                .stream(true)
                .user(fixerSpec.initialUserMessage() + "\n\n"
                    + buildFixerPrompt(runtimeOs, workspace, attempt, behaviorOutput,
                    investigation.getFinalOutput(), financialSnapshot, orderSnapshot))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            System.out.println("FIXER_ATTEMPT_" + attempt + "_OUTPUT: " + fixerResult.getFinalOutput());
            abortIfProviderUnavailable("fixer attempt " + attempt, fixerResult.getFinalOutput());

            behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
            System.out.println("ATTEMPT_" + attempt + "_VERIFICATION: " + behaviorOutput.trim());

            reviewerResult = runner.chatClient(reviewerSpec.toAgentDefinition().getId())
                .prompt()
                .stream(true)
                .user(reviewerSpec.initialUserMessage() + "\n\n"
                    + buildReviewerPrompt(runtimeOs, workspace, fixerResult.getFinalOutput(), behaviorOutput))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            System.out.println("REVIEW_ATTEMPT_" + attempt + "_OUTPUT: " + reviewerResult.getFinalOutput());
            abortIfProviderUnavailable("reviewer attempt " + attempt, reviewerResult.getFinalOutput());
            if (behaviorOutput.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        ContextResult summary = runner.chatClient(summarizerSpec.toAgentDefinition().getId())
            .prompt()
            .stream(true)
            .user(summarizerSpec.initialUserMessage() + "\n\n"
                + buildSummarizerPrompt(investigation.getFinalOutput(), fixerResult.getFinalOutput(),
                reviewerResult.getFinalOutput(), behaviorOutput))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        Assertions.assertTrue(behaviorOutput.contains("BEHAVIOR_OK"),
            "Agent must fix the bugs successfully. Verification output: " + behaviorOutput);
        Assertions.assertNotNull(investigation.getFinalOutput());
        Assertions.assertNotNull(fixerResult.getFinalOutput());
        Assertions.assertNotNull(reviewerResult.getFinalOutput());
        Assertions.assertTrue(fixerSpec.hasRequiredPatch(), fixerSpec.taskIncompleteMessage());

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

    private static String buildInvestigatorPrompt(RuntimeOs runtimeOs, Path workspace, String behaviorOutput)
    {
        return """
            Investigate root causes in FinancialCalculator.py and OrderProcessor.py.

            Current verification output:
            %s

            Required behavior contract:
            - Total should be 1958.34, not 2232.71
            - Verifier must print BEHAVIOR_OK

            Known likely bug categories to check explicitly:
            - FinancialCalculator.py: calculate_percentage may have wrong division or premature rounding
            - OrderProcessor.py: calculate_taxable_amount may have inverted shipping tax logic

            Steps:
            1) CD into the workspace.
            2) Print FinancialCalculator.py and OrderProcessor.py.
            3) Run verifier to see current output.
            4) Confirm or reject each likely bug category with evidence.
            5) Produce a concrete fixer checklist with exact functions/lines to update.

            Runtime OS: %s
            Workspace: %s
            File print commands:
            - %s
            - %s
            Verify command: %s
            """.formatted(behaviorOutput.trim(), runtimeOs.displayName, workspace,
            runtimeOs.printFileCommand("FinancialCalculator.py"), runtimeOs.printFileCommand("OrderProcessor.py"),
            runtimeOs.verifyCommand());
    }

    private static String buildFixerPrompt(RuntimeOs runtimeOs, Path workspace, int attempt,
                                           String behaviorOutput, String investigationOutput,
                                           String financialSnapshot, String orderSnapshot)
    {
        return """
            Attempt %d to fix the bugs in FinancialCalculator.py and OrderProcessor.py.

            === CRITICAL INSTRUCTIONS ===
            The investigator already identified the likely root causes. Execute the fixes instead of restarting the analysis.

            1. Read the investigator findings carefully.
            2. Apply all confirmed fixes in one or two apply_patch calls maximum.
            3. Use exact current file content when constructing the diff.
            4. After patching, run the verifier immediately.
            5. If verification still fails, re-read the files, identify the remaining wrong component, and apply one more precise patch.

            === INVESTIGATOR FINDINGS ===
            %s

            === REQUIRED BEHAVIOR CONTRACT ===
            - Total should be 1958.34, not 2232.71
            - Verifier must print BEHAVIOR_OK

            === CURRENT VERIFICATION STATUS ===
            %s

            === CURRENT FinancialCalculator.py ===
            %s

            === CURRENT OrderProcessor.py ===
            %s

            === APPLY_PATCH REMINDER ===
            Use one of these payload shapes only:
            - {"type":"update_file","path":"FinancialCalculator.py","diff":"-old\\n+new"}
            - {"operation":{"type":"update_file","path":"OrderProcessor.py","diff":"-old\\n+new"}}
            Do not use git diff headers like diff --git, ---, +++, or @@.

            === EXECUTION STEPS ===
            1. Patch the confirmed bugs.
            2. Run verifier: %s
            3. Stop only when output contains BEHAVIOR_OK.

            Runtime OS: %s
            Workspace: %s
            """.formatted(attempt, investigationOutput, behaviorOutput.trim(), financialSnapshot, orderSnapshot,
            runtimeOs.verifyCommand(), runtimeOs.displayName, workspace);
    }

    private static String buildReviewerPrompt(RuntimeOs runtimeOs, Path workspace, String fixerOutput, String behaviorOutput)
    {
        return """
            Review the fix result for FinancialCalculator.py and OrderProcessor.py.

            Fixer output:
            %s

            Latest host-side verification output:
            %s

            Execute verifier again if needed:
            %s

            Return PASS only when output contains BEHAVIOR_OK. Otherwise return FAIL with the remaining broken requirement.

            Runtime OS: %s
            Workspace: %s
            """.formatted(fixerOutput, behaviorOutput.trim(), runtimeOs.verifyCommand(), runtimeOs.displayName, workspace);
    }

    private static String buildSummarizerPrompt(String investigation, String fixerOutput,
                                                String reviewerOutput, String behaviorOutput)
    {
        return """
            Summarize what was done during this debugging session.

            === INVESTIGATION OUTPUT ===
            %s

            === FIXER OUTPUT ===
            %s

            === REVIEWER OUTPUT ===
            %s

            === FINAL VERIFICATION RESULT ===
            %s

            Provide a concise summary with:
            - Files modified: [list files]
            - Changes made: [brief description of changes]
            - Outcome: [success/failure and final verification result]
            """.formatted(investigation, fixerOutput, reviewerOutput, behaviorOutput.trim());
    }

    private static void abortIfProviderUnavailable(String stage, String output)
    {
        if (isProviderUnavailable(output))
        {
            Assumptions.assumeTrue(false,
                "Skipping test because Excalibur could not reach the real model provider during " + stage + ": " + output);
        }
    }

    private static boolean isProviderUnavailable(String output)
    {
        if (output == null || output.isBlank())
        {
            return false;
        }
        String normalized = output.toLowerCase(Locale.ROOT);
        return normalized.contains("network is unreachable")
            || normalized.contains("failed to stream from provider")
            || normalized.contains("model invocation failed after retries");
    }

    private static ExcaliburAgentSpec createInvestigatorAgent(RuntimeOs runtimeOs, Path workspace, ExcaliburTaskRequest taskRequest)
    {
        return ExcaliburAgentFactory.createSpec(
            "excalibur-investigator",
            "Excalibur Investigator",
            MODEL,
            workspace,
            ExcaliburAgentRole.INVESTIGATOR,
            taskRequest,
            "Read source + verifier output, then return a concrete evidence-backed root-cause list and fixer checklist.",
            """
            Target files: FinancialCalculator.py, OrderProcessor.py.
            Verify command: %s.
            Check explicitly whether calculate_percentage divides by 1000 instead of 100 and whether shipping tax logic is inverted.
            Do not propose unrelated refactors.
            """.formatted(runtimeOs.verifyCommand()),
            ExcaliburAgentRole.INVESTIGATOR.getDefaultToolNames(),
            List.of(),
            true,
            "low");
    }

    private static ExcaliburAgentSpec createFixerAgent(RuntimeOs runtimeOs, Path workspace, ExcaliburTaskRequest taskRequest)
    {
        return ExcaliburAgentFactory.createSpec(
            "excalibur-fixer",
            "Excalibur Fixer",
            MODEL,
            workspace,
            ExcaliburAgentRole.FIXER,
            taskRequest,
            "Read -> patch -> verify -> if still failing, re-read and apply one more targeted patch. Keep the final response concise.",
            """
            Target files: FinancialCalculator.py, OrderProcessor.py.
            Use workspace-relative file paths in apply_patch payloads.
            Verify command: %s.
            Focus on minimal edits that restore the required order total and BEHAVIOR_OK output.
            Finish only after producing a real source patch.
            """.formatted(runtimeOs.verifyCommand()),
            ExcaliburAgentRole.FIXER.getDefaultToolNames(),
            List.of(),
            true,
            "medium");
    }

    private static ExcaliburAgentSpec createReviewerAgent(RuntimeOs runtimeOs, Path workspace, ExcaliburTaskRequest taskRequest)
    {
        return ExcaliburAgentFactory.createSpec(
            "excalibur-reviewer",
            "Excalibur Reviewer",
            MODEL,
            workspace,
            ExcaliburAgentRole.REVIEWER,
            taskRequest,
            "Run or inspect the verifier output and return PASS/FAIL with direct evidence.",
            """
            Target verification command: %s.
            PASS requires the exact success marker BEHAVIOR_OK.
            """.formatted(runtimeOs.verifyCommand()),
            ExcaliburAgentRole.REVIEWER.getDefaultToolNames(),
            List.of(),
            true,
            "low");
    }

    private static ExcaliburAgentSpec createSummarizerAgent(Path workspace, ExcaliburTaskRequest taskRequest)
    {
        return ExcaliburAgentFactory.createSpec(
            "excalibur-summarizer",
            "Excalibur Summarizer",
            MODEL,
            workspace,
            ExcaliburAgentRole.SUMMARIZER,
            taskRequest,
            "Return a short structured summary with Files, Changes, Outcome, and Patch sections.",
            "Summarize the finished debugging session for engineers and mention whether the patch requirement was satisfied.",
            List.of(),
            List.of(),
            false,
            "low");
    }

    private static ExcaliburTaskRequest createTaskRequest(Path workspace) throws IOException
    {
        return ExcaliburTaskRequest.builder(
                "Fix the verification failures in FinancialCalculator.py and OrderProcessor.py and leave a non-empty patch.",
                workspace)
            .issue("Current verification reports total=2232.71 instead of 1958.34 and must end with BEHAVIOR_OK.")
            .baseCommit(resolveHeadCommit(workspace))
            .mustPatch(true)
            .patchPath(workspace.resolve("excalibur.patch"))
            .build();
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
        initializeWorkspaceGitRepository(workspace);
    }

    private static String resolveHeadCommit(Path workspace) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder("git", "rev-parse", "HEAD");
        builder.directory(workspace.toFile());
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), runtimeConsoleCharset()).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isBlank())
            {
                throw new IOException("Failed to resolve HEAD commit: " + output);
            }
            return output;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while resolving HEAD commit", ex);
        }
    }

    private static void initializeWorkspaceGitRepository(Path workspace) throws IOException
    {
        runGitCommand(workspace, "git", "init");
        runGitCommand(workspace, "git", "config", "user.email", "excalibur@example.com");
        runGitCommand(workspace, "git", "config", "user.name", "Excalibur Test");
        runGitCommand(workspace, "git", "add", "FinancialCalculator.py", "OrderProcessor.py");
        runGitCommand(workspace, "git", "commit", "-m", "Initial workspace snapshot");
    }

    private static void runGitCommand(Path workspace, String... command) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workspace.toFile());
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), runtimeConsoleCharset());
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                throw new IOException(String.join(" ", command) + " failed: " + output);
            }
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running git command", ex);
        }
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

        private String verifyCommand()
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
                    if (shown >= 5)
                    {
                        break;
                    }
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
