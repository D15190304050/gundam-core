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
import stark.dataworks.coderaider.genericagent.core.examples.ExampleStreamingPublishers;
import stark.dataworks.coderaider.genericagent.core.examples.ExampleSupport;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.SeedLlmClient;
import stark.dataworks.coderaider.genericagent.core.runner.AgentRunner;
import stark.dataworks.coderaider.genericagent.core.runner.RunConfiguration;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.LocalShellTool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * 25) Harder ReAct debug/fix workflow: logical bug fixing with runtime verification.
 * <p>
 * Pattern:
 * - Coordinator: decides delegation order.
 * - Investigator: inspects source + verification evidence.
 * - Fixer: patches iteratively and runs verification.
 * - Reviewer: validates verification result and summarizes.
 */
public class Example25ComplexReActDebugFixTest
{
    // Switch model.
    public static final String API_KEY_NAME = "MODEL_SCOPE_API_KEY";
    //        public static final String API_KEY_NAME = "VOLCENGINE_API_KEY";
//    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final String MODEL = "Qwen/Qwen3.5-27B";
    //        private static final String MODEL = "doubao-seed-code-preview-251028";
    private static final Path INPUT_FILE = Path.of("src", "test", "resources", "inputs", "InvoiceSummaryEngine.py");
    private static final Path INPUT_VERIFIER_FILE = Path.of("src", "test", "resources", "inputs", "InvoiceSummaryEngineVerifier.py");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(20, null, 0.0, 1024, "auto", "text", Map.of());

    @Test
    public void run() throws IOException
    {
        long startedAt = System.nanoTime();
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get(API_KEY_NAME, System.getenv(API_KEY_NAME));
        if (apiKey == null || apiKey.isBlank())
        {
            System.out.println("Skipping test: " + API_KEY_NAME + " not set");
            return;
        }

        RuntimeOs runtimeOs = detectRuntimeOs();
//        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example25");
        Path workspace = Path.of("D:\\DinoStark\\Projects\\CodeSpaces\\CodeRaider\\GenericAgent\\generic-agent-core\\src\\test\\resources\\outputs\\react-agent\\example25");
        Path targetFile = resetWorkspace(workspace);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRegistry agentRegistry = createAgentRegistry(runtimeOs, workspace);

        AgentRunner runner = AgentRunner.builder()
            // Switch model.
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL, false))
//            .llmClient(new SeedLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.reactThoughtActionObservation())
            .build();

//        logSourceSnapshot(targetFile, "INITIAL_SOURCE");

        String behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
        System.out.println("INITIAL_VERIFICATION: " + behaviorOutput.trim());

        ContextResult investigatorResult = runner.chatClient("react25-investigator")
            .prompt()
            .stream(true)
            .user(buildInvestigatorPrompt(runtimeOs, workspace, behaviorOutput))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        System.out.println("INVESTIGATOR_OUTPUT: " + investigatorResult.getFinalOutput());

        for (int attempt = 1; attempt <= 5; attempt++)
        {
            String sourceSnapshot = Files.readString(targetFile);
            ContextResult fixerResult = runner.chatClient("react25-fixer")
                .prompt()
                .stream(true)
                .user(buildFixerPrompt(runtimeOs, workspace, attempt, behaviorOutput, investigatorResult.getFinalOutput(), sourceSnapshot))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            System.out.println("FIXER_ATTEMPT_" + attempt + "_OUTPUT: " + fixerResult.getFinalOutput());

            behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
            System.out.println("ATTEMPT_" + attempt + "_VERIFICATION: " + behaviorOutput.trim());
//            logSourceSnapshot(targetFile, "ATTEMPT_" + attempt + "_SOURCE");
            if (behaviorOutput.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        long elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000L;
        Assertions.assertTrue(elapsedSeconds <= 120, "Expected runtime (<=120s) but took " + elapsedSeconds + "s");
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
        Path targetFile = workspace.resolve("InvoiceSummaryEngine.py");
        Files.writeString(targetFile, Files.readString(INPUT_FILE));
        stageVerifierSource(workspace.resolve("InvoiceSummaryEngineVerifier.py"));
        return targetFile;
    }

    private static AgentRegistry createAgentRegistry(RuntimeOs runtimeOs, Path workspace)
    {
        AgentRegistry registry = new AgentRegistry();
//        registry.register(createCoordinatorAgent(runtimeOs, workspace));
        registry.register(createInvestigatorAgent(runtimeOs, workspace));
        registry.register(createFixerAgent(runtimeOs, workspace));
        registry.register(createReviewerAgent(runtimeOs, workspace));
        return registry;
    }

    private static AgentDefinition createCoordinatorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-coordinator");
        def.setName("Complex ReAct Coordinator");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a workflow coordinator for code debugging.

            Build a concise execution plan and enforce this order:
            1) Investigator finds concrete bug evidence
            2) Fixer patches and verifies
            3) Reviewer validates verification output

            OS: %s
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("Plan the delegation in 3-5 short bullets with concrete commands.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
//        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createInvestigatorAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-investigator");
        def.setName("Complex ReAct Investigator");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a code bug investigator.

            Inspect source and verifier output to identify root causes.
            Read the file(s) by the local_shell tool before your investigation.
            Report exception if you can't open the file(s) to fix, and stop immediately.
            Report exact bug locations and expected behavior.

            OS: %s
            Workspace: %s
            Verify command: %s
            
            For fixing bugs, handoff to 'react25-fixer', with your investigation results.
            To handoff, respond with 'handoff: <agent_id>' where agent_id is 'react25-fixer'.
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.verifyCommand(workspace)));
        def.setReactInstructions("Read file + verifier output, then return a concrete root-cause list for each failing behavior.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setHandoffAgentIds(List.of("react25-fixer"));
//        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createFixerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-fixer");
        def.setName("Complex ReAct Fixer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a code code fixer.

            Rules:
            - Patch only InvoiceSummaryEngine.py
            - Output the plan for fixing based on the investigation before
            - Fix the root causes from investigator evidence based on the plan
            - Run verification after patching
            - Stop only when verification output contains BEHAVIOR_OK
            - Follow the coding style
            - Please follow the syntax (.py, Python) when generating code for fixing

            OS: %s
            Workspace: %s
            Verify command: %s
            
            For fix review, handoff to 'react25-reviewer', with your results.
            To handoff, respond with 'handoff: <agent_id>' where agent_id is 'react25-reviewer'.
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.verifyCommand(workspace)));
        def.setReactInstructions("Read → patch -> verify -> if fail, patch again. Keep final response concise with exact fixes.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setHandoffAgentIds(List.of("react25-reviewer"));
//        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static AgentDefinition createReviewerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-reviewer");
        def.setName("Complex ReAct Reviewer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a strict verifier for code bug-fix tasks.

            Validate with runtime evidence. PASS only when output contains BEHAVIOR_OK.

            OS: %s
            Workspace: %s
            Verify command: %s
            """.formatted(runtimeOs.displayName, workspace, runtimeOs.verifyCommand(workspace)));
        def.setReactInstructions("Run verifier and return PASS/FAIL with command output evidence.");
        def.setToolNames(List.of("local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
//        def.setModelReasoning(Map.of("effort", "low"));
        return def;
    }

    private static String buildCoordinatorPrompt(RuntimeOs runtimeOs, Path workspace, String behaviorOutput)
    {
        return """
            Build a concise execution plan for fixing InvoiceSummaryEngine.py.

            Current verification output:
            %s

            Required behavior contract:
            %s

            Require this sequence:
            - Investigator gathers evidence against the behavior contract
            - Fixer patches and verifies until BEHAVIOR_OK
            - Reviewer validates with runtime evidence

            Runtime OS: %s
            Workspace: %s
            """.formatted(behaviorOutput.trim(), expectedBehaviorContract(), runtimeOs.displayName, workspace);
    }

    private static String buildInvestigatorPrompt(RuntimeOs runtimeOs, Path workspace, String behaviorOutput)
    {
        return """
            Investigate root causes in InvoiceSummaryEngine.py.

            Current verification output:
            %s

            Required behavior contract:
            %s

            Known likely bug categories to check explicitly:
            - Subtotal loop skips first item by starting from index 1.
            - Food tax rate may be wrong (expected 0.08 for this verifier).
            - round2 may be rounding to 1 decimal instead of 2 decimals.

            Steps:
            1) CD into the workspace.
            2) Print InvoiceSummaryEngine.py
            3) Compile and run verifier
            4) Confirm or reject each likely bug category with evidence
            5) Produce a concrete fixer checklist

            Runtime OS: %s
            Workspace: %s
            File print command: %s
            Verify command: %s
            """.formatted(behaviorOutput.trim(), expectedBehaviorContract(), runtimeOs.displayName, workspace,
            runtimeOs.printFileCommand(workspace, "InvoiceSummaryEngine.py"), runtimeOs.verifyCommand(workspace));
    }

    private static String buildFixerPrompt(RuntimeOs runtimeOs, Path workspace, int attempt,
                                           String behaviorOutput, String investigationOutput, String sourceSnapshot)
    {
        return """
            Attempt %d to fix InvoiceSummaryEngine.py.

            === CRITICAL INSTRUCTIONS ===
            The investigator has already identified all the bugs. Your job is to EXECUTE the fixes, not re-analyze.
            
            1. Read the investigator's findings below carefully.
            2. Apply ALL fixes in ONE or TWO apply_patch calls maximum.
            3. Each patch must contain the EXACT lines to change (not comments, but actual code).
            4. After patching, run the verifier immediately.
            5. If the first patch attempt fails, re-read the file and try once more with correct content.

            === INVESTIGATOR FINDINGS (EXECUTE THESE FIXES) ===
            %s

            === REQUIRED BEHAVIOR CONTRACT ===
            %s

            === CURRENT VERIFICATION STATUS ===
            %s

            === CURRENT CODE ===
            %s

            === EXECUTION STEPS ===
            1. Apply patches for ALL bugs identified by investigator (use apply_patch tool)
            2. Run verifier: %s
            3. If output shows BEHAVIOR_OK, handoff to reviewer
            4. If still failing, read file again and apply one more targeted fix

            OS: %s
            Workspace: %s
            """.formatted(attempt, investigationOutput, expectedBehaviorContract(), behaviorOutput.trim(), sourceSnapshot,
            runtimeOs.verifyCommand(workspace), runtimeOs.displayName, workspace);
    }


    private static String expectedBehaviorContract()
    {
        return """
            - caseA: calculateTotal([20.0, 30.0, 50.0], "food", true) must equal 102.6
            - caseB: calculateTotal([10.0, 40.0], "book", false) must equal 52.0
            - Verifier must print BEHAVIOR_OK
            """;
    }

    private static String buildReviewerPrompt(RuntimeOs runtimeOs, Path workspace, String fixerOutput, String behaviorOutput)
    {
        return """
            Review the fix result for InvoiceSummaryEngine.py.

            Fixer output:
            %s

            Latest host-side verification output:
            %s

            Execute verifier again:
            %s

            Return PASS only when output contains BEHAVIOR_OK, else FAIL with reasons.

            OS: %s
            Workspace: %s
            """.formatted(fixerOutput, behaviorOutput.trim(), runtimeOs.verifyCommand(workspace), runtimeOs.displayName, workspace);
    }

    private static void logSourceSnapshot(Path targetFile, String prefix) throws IOException
    {
        String source = Files.readString(targetFile);
        System.out.println(prefix + ":\n" + source);
    }

    private static void stageVerifierSource(Path targetVerifierFile) throws IOException
    {
        if (!Files.exists(INPUT_VERIFIER_FILE))
        {
            throw new IOException("Missing verifier input source: " + INPUT_VERIFIER_FILE);
        }
        Files.writeString(targetVerifierFile, Files.readString(INPUT_VERIFIER_FILE));
    }

    private static String runBehaviorVerification(RuntimeOs runtimeOs, Path workspace)
    {
        ProcessBuilder builder = createBehaviorVerificationProcessBuilder(runtimeOs, workspace);
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), runtimeConsoleCharset());
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

    private static ProcessBuilder createBehaviorVerificationProcessBuilder(RuntimeOs runtimeOs, Path workspace)
    {
        return switch (runtimeOs)
        {
            case WINDOWS -> new ProcessBuilder("cmd", "/c",
                "cd /d \"" + workspace + "\" && python InvoiceSummaryEngineVerifier.py");
            case MACOS, LINUX -> new ProcessBuilder("bash", "-lc",
                "cd '" + workspace + "' && python InvoiceSummaryEngineVerifier.py");
        };
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

    public enum RuntimeOs
    {
        WINDOWS("windows"),
        MACOS("macos"),
        LINUX("linux");

        private final String displayName;

        RuntimeOs(String displayName)
        {
            this.displayName = displayName;
        }

        private String verifyCommand(Path workspace)
        {
            return "python InvoiceSummaryEngineVerifier.py";
        }

        private String printFileCommand(Path workspace, String fileName)
        {
            return switch (this)
            {
                case WINDOWS -> "type " + fileName;
                case MACOS, LINUX -> "cat " + fileName;
            };
        }
    }

    private static LocalShellTool createShellTool()
    {
        ToolDefinition definition = new ToolDefinition(
            "local_shell",
            """
            Execute a local shell command and return stdout/stderr.
            
            IMPORTANT: Each invocation runs in an independent process. 'cd' command does NOT persist across calls.
            To operate on a specific directory, combine 'cd' with your command in a single invocation using '&&'.
            
            Examples:
            - Windows: cd /d "C:\\path\\to\\dir" && dir
            - Unix: cd /path/to/dir && ls -la
            """,
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")));
        return new LocalShellTool(definition);
    }

    public static final class FileSystemEditor implements IApplyPatchEditor
    {
        private final Path root;

        public FileSystemEditor(Path root)
        {
            this.root = root;
        }

        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            try
            {
                Path path = safeResolve(operation.getPath());
                if (path.getParent() != null)
                {
                    Files.createDirectories(path.getParent());
                }
                Files.writeString(path, operation.getDiff());
                return ApplyPatchResult.completed("Created " + operation.getPath());
            }
            catch (IOException ex)
            {
                return ApplyPatchResult.failed("Create failed: " + ex.getMessage());
            }
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            try
            {
                Path path = safeResolve(operation.getPath());
                if (!Files.exists(path))
                {
                    return ApplyPatchResult.failed("File not found: " + operation.getPath());
                }
                String diff = operation.getDiff();
                if (containsUnsupportedDiffMarkers(diff))
                {
                    return ApplyPatchResult.failed("Unsupported diff format. Use simple diff only with context/'-'/'+' lines.");
                }
                if (!hasExplicitChangeLines(diff))
                {
                    return ApplyPatchResult.failed("Invalid diff: must include '-' and '+' change lines.");
                }
                String original = Files.readString(path);
                String updated = applySmartDiff(original, diff);

                // Critical: Check if any actual changes were applied
                if (updated.equals(original))
                {
                    return ApplyPatchResult.failed(buildNoChangeAppliedError(diff));
                }

                Files.writeString(path, updated);
                return ApplyPatchResult.completed("Updated " + operation.getPath());
            }
            catch (Exception ex)
            {
                return ApplyPatchResult.failed("Update failed: " + ex.getMessage());
            }
        }

        private static String buildNoChangeAppliedError(String diff)
        {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Diff failed: NO CHANGES were applied to the file.\n\n");
            errorMsg.append("Your diff content:\n");
            if (diff != null)
            {
                String[] lines = diff.split("\\R", -1);
                int shown = 0;
                for (String line : lines)
                {
                    if (shown >= 6) break;
                    if (line.startsWith("-") || line.startsWith("+"))
                    {
                        String display = line.length() > 80 ? line.substring(0, 80) + "..." : line;
                        errorMsg.append("  ").append(display).append("\n");
                        shown++;
                    }
                }
            }
            errorMsg.append("\nPossible reasons:\n");
            errorMsg.append("1. The '-' content does NOT exist in the file (check exact spacing/indentation).\n");
            errorMsg.append("2. The '-' and '+' lines are IDENTICAL (no actual change).\n");
            errorMsg.append("3. The file was already modified and content has changed.\n");
            errorMsg.append("\nTo fix: Read the file again, then provide a correct diff.\n");
            return errorMsg.toString();
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

        private Path safeResolve(String relativePath)
        {
            Path raw = Path.of(relativePath == null ? "" : relativePath).normalize();
            Path candidate;
            if (raw.isAbsolute())
            {
                candidate = raw;
            }
            else
            {
                candidate = root.resolve(raw).normalize();
            }
            if (candidate.startsWith(root) && Files.exists(candidate))
            {
                return candidate;
            }

            // Graceful fallback for model-generated long paths: keep file name in workspace.
            Path fileName = raw.getFileName();
            if (fileName != null)
            {
                Path byName = root.resolve(fileName).normalize();
                if (byName.startsWith(root))
                {
                    return byName;
                }
            }
            throw new IllegalArgumentException("Path escapes workspace: " + relativePath);
        }

        private static boolean hasExplicitChangeLines(String diff)
        {
            if (diff == null || diff.isBlank())
            {
                return false;
            }
            boolean hasMinus = false;
            boolean hasPlus = false;
            String[] lines = diff.split("\\R", -1);
            for (String line : lines)
            {
                if (line.startsWith("-"))
                {
                    hasMinus = true;
                }
                else if (line.startsWith("+"))
                {
                    hasPlus = true;
                }
            }
            return hasMinus && hasPlus;
        }

        private static boolean containsUnsupportedDiffMarkers(String diff)
        {
            if (diff == null)
            {
                return false;
            }
            return diff.contains("diff --git")
                || diff.contains("\n--- ")
                || diff.contains("\n+++ ")
                || diff.contains("\n@@");
        }

        private static String applySmartDiff(String original, String diff)
        {
            // Validate diff format first
            validateDiffFormat(diff);

            if (looksLikeReplacementOnlyDiff(diff))
            {
                return applySimpleReplacementDiff(original, diff);
            }
            try
            {
                String result = ApplyPatchTool.applyDiff(original, diff);
                // Verify the result is valid (not corrupted)
                if (result != null && !result.equals(original))
                {
                    validateResultNotCorrupted(original, result);
                }
                return result;
            }
            catch (Exception ignored)
            {
                return applySimpleReplacementDiff(original, diff);
            }
        }

        /**
         * Validate diff format to catch common mistakes.
         */
        private static void validateDiffFormat(String diff)
        {
            if (diff == null || diff.isBlank())
            {
                throw new IllegalArgumentException("Empty diff.");
            }

            String[] lines = diff.split("\\R", -1);

            for (String line : lines)
            {
                if (line.isBlank()) continue;

                // Check for lines that look like code but don't start with - or +
                if (!line.startsWith("-") && !line.startsWith("+"))
                {
                    if (line.startsWith(" ") || line.startsWith("\t"))
                    {
                        throw new IllegalArgumentException(
                            "INVALID DIFF FORMAT!\n\n" +
                                "Problem: Line '" + line.substring(0, Math.min(40, line.length())) + "...' starts with whitespace.\n\n" +
                                "SOLUTION: For simple diff, EVERY line must start with '-' (old) or '+' (new).\n\n" +
                                "Example of CORRECT diff to change a return value:\n" +
                                "  -        return 0.18\n" +
                                "  +        return 0.08\n\n" +
                                "Do NOT include lines that don't change. Only include the ACTUAL lines being modified."
                        );
                    }
                }
            }
            // Note: We don't check for identical '-' and '+' lines here because
            // applySimpleReplacementDiff will automatically skip them.
        }

        /**
         * Check if the result file is corrupted (e.g., structure destroyed).
         */
        private static void validateResultNotCorrupted(String original, String result)
        {
            // Check if the result has reasonable structure
            // If original had imports/functions, result should too
            long originalFuncCount = countOccurrences(original, "def ");
            long resultFuncCount = countOccurrences(result, "def ");

            if (resultFuncCount < originalFuncCount)
            {
                throw new IllegalArgumentException(
                    "Diff would corrupt the file: function definitions would be lost.\n" +
                        "Original had " + originalFuncCount + " function(s), result would have " + resultFuncCount + ".\n" +
                        "Please verify your diff is correct and targets the right lines."
                );
            }

            // Check for Python syntax issues (indentation at file start)
            String[] resultLines = result.split("\\R", -1);
            if (resultLines.length > 0)
            {
                String firstLine = resultLines[0];
                if (firstLine.startsWith("    ") || firstLine.startsWith("\t"))
                {
                    // First line should not be indented in a Python file
                    if (original.split("\\R", -1)[0].startsWith("import ") ||
                        original.split("\\R", -1)[0].startsWith("def ") ||
                        original.split("\\R", -1)[0].startsWith("class ") ||
                        original.split("\\R", -1)[0].startsWith("#") ||
                        original.split("\\R", -1)[0].isBlank())
                    {
                        throw new IllegalArgumentException(
                            "Diff would corrupt the file: first line would be incorrectly indented.\n" +
                                "First line of result: '" + firstLine.substring(0, Math.min(50, firstLine.length())) + "...'\n" +
                                "Please verify your diff targets the correct location in the file."
                        );
                    }
                }
            }
        }

        private static long countOccurrences(String text, String substring)
        {
            long count = 0;
            int index = 0;
            while ((index = text.indexOf(substring, index)) != -1)
            {
                count++;
                index += substring.length();
            }
            return count;
        }

        private static boolean looksLikeReplacementOnlyDiff(String diff)
        {
            if (diff == null || diff.isBlank())
            {
                return false;
            }
            boolean hasMinus = false;
            boolean hasPlus = false;
            String[] lines = diff.split("\\R", -1);
            for (String line : lines)
            {
                if (line.isBlank())
                {
                    continue;
                }
                if (line.startsWith("-"))
                {
                    hasMinus = true;
                    continue;
                }
                if (line.startsWith("+"))
                {
                    hasPlus = true;
                    continue;
                }
                return false;
            }
            return hasMinus && hasPlus;
        }

        private static String applySimpleReplacementDiff(String original, String diff)
        {
            if (diff == null || diff.isBlank())
            {
                throw new IllegalArgumentException("Empty diff.");
            }

            // Parse diff into pairs of (old, new)
            List<String[]> diffPairs = new ArrayList<>();
            String[] diffLines = diff.split("\\R", -1);
            String pendingOld = null;
            int skippedCount = 0;

            for (String line : diffLines)
            {
                if (line.startsWith("-"))
                {
                    pendingOld = line.substring(1);
                    continue;
                }
                if (line.startsWith("+") && pendingOld != null)
                {
                    String newContent = line.substring(1);
                    // Skip if old and new are identical
                    if (pendingOld.equals(newContent))
                    {
                        skippedCount++;
                    }
                    else
                    {
                        diffPairs.add(new String[]{pendingOld, newContent});
                    }
                    pendingOld = null;
                    continue;
                }
                if (!line.isBlank())
                {
                    pendingOld = null;
                }
            }

            if (diffPairs.isEmpty())
            {
                if (skippedCount > 0)
                {
                    throw new IllegalArgumentException(
                        "Diff contains only identical '-old' and '+new' pairs. No actual changes detected."
                    );
                }
                throw new IllegalArgumentException("No valid diff pairs found.");
            }

            // Apply replacements LINE-BY-LINE for accuracy
            String[] originalLines = original.split("\\R", -1);
            List<String> resultLines = new ArrayList<>();
            for (String originalLine : originalLines)
            {
                String currentLine = originalLine;

                // Try to match and replace for each diff pair
                for (String[] pair : diffPairs)
                {
                    String oldContent = pair[0];
                    String newContent = pair[1];

                    // Check if this line contains the old content
                    int idx = currentLine.indexOf(oldContent);
                    if (idx >= 0)
                    {
                        // Verify this is a reasonable match (not a partial match)
                        // The old content should match a substantial part of the line
                        currentLine = currentLine.substring(0, idx) + newContent + currentLine.substring(idx + oldContent.length());
                    }
                }
                resultLines.add(currentLine);
            }

            // Check if any changes were made
            String result = String.join("\n", resultLines);
            if (result.equals(original))
            {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("Diff failed: NO changes were applied.\n\n");
                errorMsg.append("The following content was NOT found in any line:\n");
                for (int i = 0; i < Math.min(3, diffPairs.size()); i++)
                {
                    String old = diffPairs.get(i)[0];
                    String display = old.length() > 80 ? old.substring(0, 80) + "..." : old;
                    errorMsg.append("  \"").append(display).append("\"\n");
                }
                errorMsg.append("\nTo fix: Read the file again and provide exact matching content.");
                throw new IllegalArgumentException(errorMsg.toString());
            }

            return result;
        }
    }
}
