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
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ApplyPatchTool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * 25) Harder ReAct debug/fix workflow: logical bug fixing with runtime verification.
 * <p>
 * Pattern:
 * - Single debugger agent: inspect, patch, verify by tool calls.
 * - Outer retry loop: rerun agent if verifier still fails.
 */
public class Example25ComplexReActDebugFixTest
{
    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final Path INPUT_FILE = Path.of("src", "test", "resources", "inputs", "InvoiceSummaryEngine.java");
    private static final Path INPUT_VERIFIER_FILE = Path.of("src", "test", "resources", "inputs", "InvoiceSummaryEngineVerifier.java");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(2, null, 0.0, 256, "auto", "text", Map.of());

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
        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example25");
        Path targetFile = resetWorkspace(workspace);

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(createDebuggerAgent(runtimeOs, workspace));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.reactThoughtActionObservation())
            .build();

        String behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
        System.out.println("INITIAL_VERIFICATION: " + behaviorOutput.trim());

        ContextResult debuggerResult = null;
        for (int attempt = 1; attempt <= 3; attempt++)
        {
            String sourceSnapshot = Files.readString(targetFile);
            debuggerResult = runner.chatClient("react25-debugger")
                .prompt()
                .stream(true)
                .user(buildDebuggerPrompt(runtimeOs, workspace, attempt, behaviorOutput, sourceSnapshot))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            behaviorOutput = runBehaviorVerification(runtimeOs, workspace);
            System.out.println("ATTEMPT_" + attempt + "_VERIFICATION: " + behaviorOutput.trim());
            if (behaviorOutput.contains("BEHAVIOR_OK"))
            {
                break;
            }
        }

        Assertions.assertNotNull(debuggerResult, "Expected debugger output");
        Assertions.assertTrue(debuggerResult.getFinalOutput() != null && !debuggerResult.getFinalOutput().isBlank(),
            "Expected debugger summary output");
        Assertions.assertTrue(behaviorOutput.contains("BEHAVIOR_OK"), "Expected runtime verification output: " + behaviorOutput);
        System.out.println("FINAL_VERIFICATION: " + behaviorOutput.trim());
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
        Path targetFile = workspace.resolve("InvoiceSummaryEngine.java");
        Files.writeString(targetFile, Files.readString(INPUT_FILE));
        stageVerifierSource(workspace.resolve("InvoiceSummaryEngineVerifier.java"));
        return targetFile;
    }

    private static Agent createDebuggerAgent(RuntimeOs runtimeOs, Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-debugger");
        def.setName("Complex ReAct Debugger");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are a Java debugging agent working in a local workspace.

            Goal:
            Make verifier output include BEHAVIOR_OK with:
            - caseA = 102.6
            - caseB = 52.0

            Known defects in this scenario to verify and fix:
            1) subtotal loop starts at 1 and should start at 0.
            2) food tax is 0.18 and should be 0.08.
            3) round2 uses *10.0 and should use *100.0.

            Constraints:
            - Use apply_patch for edits.
            - Use paths relative to workspace in apply_patch operations.
            - Use simple diff lines (context / '-' remove / '+' add).
            - Avoid git/unified headers (diff --git, ---, +++, @@).
            - Prefer one minimal patch that fixes all identified defects.

            apply_patch format:
            {"operation":{"type":"update_file","path":"...","diff":"..."}}

            Simple diff example:
            -        for (int i = 1; i < items.length; i++) {
            +        for (int i = 0; i < items.length; i++) {
            ...
            -            return 0.18;
            +            return 0.08;
            ...
            -        return Math.round(value * 10.0) / 10.0;
            +        return Math.round(value * 100.0) / 100.0;

            Runtime OS: %s
            Workspace: %s
            """.formatted(runtimeOs.displayName, workspace));
        def.setReactInstructions("Think briefly, then patch. Keep output concise: root causes + patch summary.");
        def.setToolNames(List.of("apply_patch"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static String buildDebuggerPrompt(RuntimeOs runtimeOs, Path workspace, int attempt, String behaviorOutput, String sourceSnapshot)
    {
        return """
            Debug attempt: %d

            Current verifier output:
            %s

            Expected behavior:
            - caseA should become 102.6
            - caseB should become 52.0
            - output should include BEHAVIOR_OK
            - fix aggregation logic, tax logic, and rounding logic.

            Current source snapshot (InvoiceSummaryEngine.java):
            %s

            Please patch based on the verifier output and source snapshot. External verification will run after your response.
            """.formatted(attempt, behaviorOutput, sourceSnapshot);
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
                "cd /d \"" + workspace + "\" && javac InvoiceSummaryEngine.java InvoiceSummaryEngineVerifier.java && java InvoiceSummaryEngineVerifier");
            case MACOS, LINUX -> new ProcessBuilder("bash", "-lc",
                "cd '" + workspace + "' && javac InvoiceSummaryEngine.java InvoiceSummaryEngineVerifier.java && java InvoiceSummaryEngineVerifier");
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
                Files.writeString(path, updated);
                return ApplyPatchResult.completed("Updated " + operation.getPath());
            }
            catch (Exception ex)
            {
                return ApplyPatchResult.failed("Update failed: " + ex.getMessage());
            }
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
            if (looksLikeReplacementOnlyDiff(diff))
            {
                return applySimpleReplacementDiff(original, diff);
            }
            try
            {
                return ApplyPatchTool.applyDiff(original, diff);
            }
            catch (Exception ignored)
            {
                return applySimpleReplacementDiff(original, diff);
            }
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

            String[] lines = diff.split("\\R", -1);
            String updated = original;
            String pendingRemoved = null;
            int replacedCount = 0;
            for (String line : lines)
            {
                if (line.startsWith("-"))
                {
                    pendingRemoved = line.substring(1);
                    continue;
                }
                if (line.startsWith("+") && pendingRemoved != null)
                {
                    String added = line.substring(1);
                    int index = updated.indexOf(pendingRemoved);
                    if (index >= 0)
                    {
                        updated = updated.substring(0, index) + added + updated.substring(index + pendingRemoved.length());
                        replacedCount++;
                    }
                    pendingRemoved = null;
                    continue;
                }
                if (!line.isBlank())
                {
                    pendingRemoved = null;
                }
            }
            if (replacedCount == 0)
            {
                throw new IllegalArgumentException("Simple replacement diff did not match any content.");
            }
            return updated;
        }
    }
}
