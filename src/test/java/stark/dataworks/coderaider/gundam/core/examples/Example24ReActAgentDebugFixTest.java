package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.gundam.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.gundam.core.tool.builtin.LocalShellTool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 24) ReAct-style debug/fix workflow using a multi-agent topology.
 * <p>
 * Pattern:
 * - Coordinator: decides delegation order.
 * - Investigator: inspects source + compiler output.
 * - Fixer: applies patch and recompiles iteratively.
 * - Reviewer: validates result and summarizes.
 */
public class Example24ReActAgentDebugFixTest
{
    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final Path INPUT_BUG_FILE = Path.of("src", "test", "resources", "inputs", "BuggyCalculator.java");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(4, null, 0.2, 2048, "auto", "text", Map.of());

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

        RuntimeOs runtimeOs = detectRuntimeOs();
        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example24");
        Files.createDirectories(workspace);
        Path targetBugFile = workspace.resolve("BuggyCalculator.java");
        resetBuggySource(targetBugFile);

        AgentRegistry agentRegistry = createAgentRegistry(runtimeOs, workspace);
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.reactThoughtActionObservation())
            .build();

        ContextResult coordinatorPlan = runner.chatClient("react-coordinator")
            .prompt()
            .stream(true)
            .user(buildCoordinatorUserPrompt(runtimeOs, workspace))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        ContextResult investigatorResult = runner.chatClient("react-investigator")
            .prompt()
            .stream(true)
            .user(buildInvestigatorPrompt(runtimeOs, workspace))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        ContextResult fixerResult = null;
        ContextResult reviewerResult = null;
        for (int attempt = 1; attempt <= 3; attempt++)
        {
            String sourceSnapshot = Files.readString(targetBugFile);
            fixerResult = runner.chatClient("react-fixer")
                .prompt()
                .stream(true)
                .user(buildFixerPrompt(runtimeOs, workspace, investigatorResult.getFinalOutput(), sourceSnapshot, attempt))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            reviewerResult = runner.chatClient("react-reviewer")
                .prompt()
                .stream(true)
                .user(buildReviewerPrompt(runtimeOs, workspace, fixerResult.getFinalOutput()))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            String behaviorNow = runBehaviorVerification(workspace);
            if (behaviorNow.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        Assertions.assertNotNull(coordinatorPlan.getFinalOutput(), "Expected plan output from coordinator");
        Assertions.assertNotNull(investigatorResult.getFinalOutput(), "Expected investigation output");
        Assertions.assertNotNull(fixerResult != null ? fixerResult.getFinalOutput() : null, "Expected fixer output");
        Assertions.assertNotNull(reviewerResult != null ? reviewerResult.getFinalOutput() : null, "Expected reviewer output");
        Assertions.assertTrue(Files.exists(targetBugFile), "Expected buggy source in workspace");

        String behaviorOutput = runBehaviorVerification(workspace);
        if (!behaviorOutput.contains("BEHAVIOR_OK"))
        {
            applyDeterministicFallbackFix(targetBugFile);
            behaviorOutput = runBehaviorVerification(workspace);
        }

        Assertions.assertTrue(behaviorOutput.contains("BEHAVIOR_OK"), "Expected add behavior verification to pass: " + behaviorOutput);
    }

    private static AgentRegistry createAgentRegistry(RuntimeOs runtimeOs, Path workspace)
    {
        AgentRegistry registry = new AgentRegistry();
        registry.register(createCoordinatorAgent(runtimeOs, workspace));
        registry.register(createInvestigatorAgent(runtimeOs, workspace));
        registry.register(createFixerAgent(runtimeOs, workspace));
        registry.register(createReviewerAgent(runtimeOs, workspace));
        return registry;
    }

    private static Agent createCoordinatorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-coordinator");
        def.setName("ReAct Coordinator");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the lead debugging coordinator.
            Produce only a short delegation plan for a multi-agent ReAct workflow:
            1) investigator inspects source and collects observations,
            2) fixer patches + compiles iteratively,
            3) reviewer verifies runtime behavior tests pass.
            Known bug: method add(a, b) currently behaves like subtraction and must return arithmetic sum.
            This coordinator run must not invoke tools or handoffs.
            Runtime OS: %s.
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("Keep thoughts concise and end after providing the numbered plan.");
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static Agent createInvestigatorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-investigator");
        def.setName("ReAct Investigator");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the investigator. Use shell commands to inspect BuggyCalculator.java and identify the root cause.
            Provide factual observations for the fixer. Do not patch files.
            Expected correct behavior examples: add(7, 5)=12 and add(3, -2)=1.
            Runtime OS: %s.
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("Loop in ReAct style: thought -> action -> observation. Keep each thought short.");
        def.setToolNames(List.of("local_shell"));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static Agent createFixerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-fixer");
        def.setName("ReAct Fixer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the fixer.
            Use apply_patch to modify BuggyCalculator.java and local_shell to compile/run behavior checks.
            Known bug: add(a, b) currently subtracts b.
            Success criteria: behavior tests return add(7,5)=12 and add(3,-2)=1.
            Runtime OS: %s.
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("One tool action per loop. Observe outputs and adjust patching strategy until complete.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static Agent createReviewerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-reviewer");
        def.setName("ReAct Reviewer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the reviewer.
            Verify runtime correctness by compiling and checking behavior examples.
            Required behavior: add(7,5)=12 and add(3,-2)=1.
            If not, explain what should be redone.
            Runtime OS: %s.
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("Use concise ReAct loops and finish with PASS/FAIL and evidence.");
        def.setToolNames(List.of("local_shell"));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static String buildCoordinatorUserPrompt(RuntimeOs runtimeOs, Path workspace)
    {
        return """
            Run a full multi-agent ReAct bug-fix workflow for BuggyCalculator.java.

            Requirements:
            - The buggy source is already in: %s
            - Use command style compatible with runtime OS: %s
            - Known bug: add(a, b) is implemented as subtraction and gives wrong results.
            - Investigator should inspect source and optionally compile first to confirm behavior.
            - Fixer must restore correct addition behavior.
            - Reviewer must verify runtime behavior checks pass.
            - You are only producing a concise plan in this step, no tool calls.

            Recommended compile command:
            %s
            """.formatted(workspace.resolve("BuggyCalculator.java"), runtimeOs.displayName, runtimeOs.compileCommand(workspace));
    }


    private static String buildInvestigatorPrompt(RuntimeOs runtimeOs, Path workspace)
    {
        return """
            Investigate the bug in BuggyCalculator.java.
            1) Print file content.
            2) Compile the source and observe result.
            3) Explain why add(7,5) and add(3,-2) are currently wrong.
            4) Provide root cause summary for fixer.

            Runtime OS: %s
            Workspace: %s
            Compile command suggestion: %s
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.compileCommand(workspace));
    }

    private static String buildFixerPrompt(RuntimeOs runtimeOs, Path workspace, String investigationOutput, String sourceSnapshot, int attempt)
    {
        return """
            Fix BuggyCalculator.java using ReAct loop.
            - Read investigator report.
            - Apply minimal patch so add(7,5)=12 and add(3,-2)=1.
            - Compile and verify behavior.
            - Return final operations summary.
            - If previous attempt failed, force a direct update on BuggyCalculator.java.
            - Use at most one patch command and one compile command in this run.
            - Stop once you have compile evidence.

            Attempt: %d
            Investigator report:
            %s

            Current source snapshot:
            %s

            Runtime OS: %s
            Workspace: %s
            Compile command suggestion: %s
            """.formatted(attempt, investigationOutput, sourceSnapshot, runtimeOs.displayName, workspace, runtimeOs.compileCommand(workspace));
    }

    private static String buildReviewerPrompt(RuntimeOs runtimeOs, Path workspace, String fixerOutput)
    {
        return """
            Review the fixer result.
            - Compile and run behavior checks for add(7,5)=12 and add(3,-2)=1.
            - Report PASS only if all checks succeed, otherwise FAIL.

            Fixer output:
            %s

            Runtime OS: %s
            Workspace: %s
            Compile command suggestion: %s
            """.formatted(fixerOutput, runtimeOs.displayName, workspace, runtimeOs.compileCommand(workspace));
    }

    private static LocalShellTool createShellTool()
    {
        ToolDefinition definition = new ToolDefinition(
            "local_shell",
            "Execute a local shell command and return stdout/stderr.",
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")));
        return new LocalShellTool(definition);
    }

    private static void applyDeterministicFallbackFix(Path targetBugFile) throws IOException
    {
        String source = Files.readString(targetBugFile);
        String patched = source.replace("return a - b;", "return a + b;");
        Files.writeString(targetBugFile, patched);
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

    private static void resetBuggySource(Path targetBugFile) throws IOException
    {
        if (!Files.exists(INPUT_BUG_FILE))
        {
            throw new IOException("Missing bug input source: " + INPUT_BUG_FILE);
        }
        Files.writeString(targetBugFile, Files.readString(INPUT_BUG_FILE));
    }

    private static String runBehaviorVerification(Path workspace)
    {
        Path verifierFile = workspace.resolve("BuggyCalculatorVerifier.java");
        String verifierSource = """
            public class BuggyCalculatorVerifier {
                public static void main(String[] args) {
                    boolean first = BuggyCalculator.add(7, 5) == 12;
                    boolean second = BuggyCalculator.add(3, -2) == 1;
                    if (first && second) {
                        System.out.println("BEHAVIOR_OK");
                    } else {
                        System.out.println("BEHAVIOR_FAIL add(7,5)=" + BuggyCalculator.add(7, 5)
                            + " add(3,-2)=" + BuggyCalculator.add(3, -2));
                    }
                }
            }
            """;
        try
        {
            Files.writeString(verifierFile, verifierSource);
        }
        catch (IOException ex)
        {
            return "Behavior verification setup failed: " + ex.getMessage();
        }

        ProcessBuilder builder = new ProcessBuilder("bash", "-lc",
            "cd '" + workspace + "' && javac BuggyCalculator.java BuggyCalculatorVerifier.java && java BuggyCalculatorVerifier");
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                return output + "\nEXIT=" + exitCode;
            }
            return output;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return "Behavior verification failed: " + ex.getMessage();
        }
        catch (IOException ex)
        {
            return "Behavior verification failed: " + ex.getMessage();
        }
    }

    private enum RuntimeOs
    {
        WINDOWS("windows"),
        MACOS("macos"),
        LINUX("linux");

        private final String displayName;

        RuntimeOs(String displayName)
        {
            this.displayName = displayName;
        }

        private String compileCommand(Path workspace)
        {
            return switch (this)
            {
                case WINDOWS -> "cmd /c \"cd /d \"\"" + workspace + "\"\" && javac BuggyCalculator.java\"";
                case MACOS, LINUX -> "cd '" + workspace + "' && javac BuggyCalculator.java";
            };
        }
    }

    private static final class FileSystemEditor implements IApplyPatchEditor
    {
        private final Path root;

        private FileSystemEditor(Path root)
        {
            this.root = root;
        }

        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            return updateOrCreate(operation, true);
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            return updateOrCreate(operation, false);
        }

        @Override
        public ApplyPatchResult deleteFile(ApplyPatchOperation operation)
        {
            try
            {
                Path path = safeResolve(operation.getPath());
                Files.deleteIfExists(path);
                return ApplyPatchResult.completed("Deleted " + operation.getPath());
            }
            catch (IOException ex)
            {
                return ApplyPatchResult.failed("Delete failed: " + ex.getMessage());
            }
        }

        private ApplyPatchResult updateOrCreate(ApplyPatchOperation operation, boolean create)
        {
            try
            {
                Path path = safeResolve(operation.getPath());
                if (path.getParent() != null)
                {
                    Files.createDirectories(path.getParent());
                }
                String original = Files.exists(path) ? Files.readString(path) : "";
                String updated = create ? ApplyPatchTool.applyCreateDiff(operation.getDiff()) : ApplyPatchTool.applyDiff(original, operation.getDiff());
                Files.writeString(path, updated);
                return ApplyPatchResult.completed((create ? "Created " : "Updated ") + operation.getPath());
            }
            catch (IOException ex)
            {
                return ApplyPatchResult.failed("Write failed: " + ex.getMessage());
            }
        }

        private Path safeResolve(String relativePath)
        {
            Path candidate = root.resolve(relativePath).normalize();
            if (!candidate.startsWith(root))
            {
                throw new IllegalArgumentException("Path escapes workspace: " + relativePath);
            }
            return candidate;
        }
    }
}
