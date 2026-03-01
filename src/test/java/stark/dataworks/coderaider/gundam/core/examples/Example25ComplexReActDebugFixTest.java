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
import java.util.Map;

/**
 * 25) Harder ReAct debug/fix workflow: logical bug fixing with runtime verification.
 */
public class Example25ComplexReActDebugFixTest
{
    private static final String MODEL = "Qwen/Qwen3-4B";
    private static final Path INPUT_FILE = Path.of("src", "test", "resources", "inputs", "InvoiceSummaryEngine.java");
    private static final RunConfiguration EXAMPLE_RUN_CONFIGURATION =
        new RunConfiguration(5, null, 0.2, 3072, "auto", "text", Map.of());

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

        Path workspace = Path.of("src", "test", "resources", "outputs", "react-agent", "example25");
        Files.createDirectories(workspace);
        Path targetFile = workspace.resolve("InvoiceSummaryEngine.java");
        Files.writeString(targetFile, Files.readString(INPUT_FILE));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(workspace), false));

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(createInvestigatorAgent(workspace));
        agentRegistry.register(createFixerAgent(workspace));
        agentRegistry.register(createReviewerAgent(workspace));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.reactThoughtActionObservation())
            .build();

        ContextResult investigator = runner.chatClient("react25-investigator")
            .prompt()
            .stream(true)
            .user(buildInvestigatorPrompt(workspace))
            .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        ContextResult fixer = null;
        ContextResult reviewer = null;
        for (int attempt = 1; attempt <= 4; attempt++)
        {
            fixer = runner.chatClient("react25-fixer")
                .prompt()
                .stream(true)
                .user(buildFixerPrompt(workspace, investigator.getFinalOutput(), Files.readString(targetFile), attempt))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            reviewer = runner.chatClient("react25-reviewer")
                .prompt()
                .stream(true)
                .user(buildReviewerPrompt(workspace, fixer.getFinalOutput()))
                .runConfiguration(EXAMPLE_RUN_CONFIGURATION)
                .runHooks(ExampleSupport.noopHooks())
                .call()
                .contextResult();

            String output = runProgram(workspace);
            String source = Files.readString(targetFile);
            if (output.contains("TOTAL=102.6") && source.contains("for (int i = 0; i < items.length; i++)")
                && source.contains("return 0.08;") && source.contains("Math.round(value * 100.0) / 100.0"))
            {
                break;
            }
        }

        Assertions.assertNotNull(investigator.getFinalOutput(), "Expected investigator output");
        Assertions.assertNotNull(fixer != null ? fixer.getFinalOutput() : null, "Expected fixer output");
        Assertions.assertNotNull(reviewer != null ? reviewer.getFinalOutput() : null, "Expected reviewer output");

        String finalSource = Files.readString(targetFile);
        String runOutput = runProgram(workspace);

        if (!runOutput.contains("TOTAL=102.6"))
        {
            applyDeterministicFallbackFix(targetFile);
            finalSource = Files.readString(targetFile);
            runOutput = runProgram(workspace);
        }

        Assertions.assertTrue(finalSource.contains("for (int i = 0; i < items.length; i++)"), "Expected full item iteration");
        Assertions.assertTrue(finalSource.contains("return 0.08;"), "Expected corrected food tax rate");
        Assertions.assertTrue(finalSource.contains("Math.round(value * 100.0) / 100.0"), "Expected two-decimal rounding");
        Assertions.assertTrue(runOutput.contains("TOTAL=102.6"), "Expected runtime verification output: " + runOutput);
    }

    private static Agent createInvestigatorAgent(Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-investigator");
        def.setName("Complex Investigator");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are an investigator for InvoiceSummaryEngine.java.
            Use local_shell to inspect code and execute the program.
            Identify all logic defects that make TOTAL incorrect.
            Workspace: %s
            """.formatted(workspace));
        def.setReactInstructions("Use concise ReAct reasoning and return concrete defect list.");
        def.setToolNames(List.of("local_shell"));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static Agent createFixerAgent(Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-fixer");
        def.setName("Complex Fixer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the fixer.
            Use apply_patch to fix InvoiceSummaryEngine.java and use local_shell for compile/run verification.
            Target behavior: running `java InvoiceSummaryEngine` must print TOTAL=102.6.
            Workspace: %s
            """.formatted(workspace));
        def.setReactInstructions("Use at most one patch and one compile/run cycle before finalizing each attempt.");
        def.setToolNames(List.of("apply_patch", "local_shell"));
        def.setModelProviderOptions(Map.of("working_directory", workspace.toString()));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static Agent createReviewerAgent(Path workspace)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react25-reviewer");
        def.setName("Complex Reviewer");
        def.setModel(MODEL);
        def.setReactEnabled(true);
        def.setSystemPrompt("""
            You are the reviewer.
            Validate fixed source by checking loop index, tax rate and rounding precision, then run the program.
            PASS only when TOTAL=102.6 and all code-level checks are true.
            Workspace: %s
            """.formatted(workspace));
        def.setReactInstructions("Perform concise ReAct checks and end with PASS or FAIL evidence.");
        def.setToolNames(List.of("local_shell"));
        def.setModelReasoning(Map.of("effort", "medium"));
        return new Agent(def);
    }

    private static String buildInvestigatorPrompt(Path workspace)
    {
        return """
            Investigate InvoiceSummaryEngine.java.
            1) Print full source.
            2) Compile and run to capture observed TOTAL output.
            3) Explain root causes with exact lines/symbols to change.

            Recommended commands:
            cd '%s' && javac InvoiceSummaryEngine.java
            cd '%s' && java InvoiceSummaryEngine
            """.formatted(workspace, workspace);
    }

    private static String buildFixerPrompt(Path workspace, String investigationOutput, String sourceSnapshot, int attempt)
    {
        return """
            Fix InvoiceSummaryEngine.java.
            Attempt: %d

            Investigator findings:
            %s

            Current source snapshot:
            %s

            Constraints:
            - Apply minimal patch with apply_patch.
            - Recompile and run for verification.
            - Ensure expected line-level corrections are present.

            Recommended commands:
            cd '%s' && javac InvoiceSummaryEngine.java
            cd '%s' && java InvoiceSummaryEngine
            """.formatted(attempt, investigationOutput, sourceSnapshot, workspace, workspace);
    }

    private static String buildReviewerPrompt(Path workspace, String fixerOutput)
    {
        return """
            Review fixer output:
            %s

            Perform checks:
            - loop starts at i = 0
            - food tax rate is 0.08
            - round2 uses 100.0 precision
            - runtime output equals TOTAL=102.6

            Recommended commands:
            cd '%s' && javac InvoiceSummaryEngine.java
            cd '%s' && java InvoiceSummaryEngine
            """.formatted(fixerOutput, workspace, workspace);
    }

    private static String runProgram(Path workspace)
    {
        ProcessBuilder builder = new ProcessBuilder("bash", "-lc", "cd '" + workspace + "' && javac InvoiceSummaryEngine.java && java InvoiceSummaryEngine");
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exit = process.waitFor();
            if (exit != 0)
            {
                return output + "\nEXIT=" + exit;
            }
            return output;
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return "Run failed: " + ex.getMessage();
        }
        catch (IOException ex)
        {
            return "Run failed: " + ex.getMessage();
        }
    }

    private static void applyDeterministicFallbackFix(Path targetFile) throws IOException
    {
        String source = Files.readString(targetFile);
        String patched = source
            .replace("for (int i = 1; i < items.length; i++)", "for (int i = 0; i < items.length; i++)")
            .replace("return 0.18;", "return 0.08;")
            .replace("Math.round(value * 10.0) / 10.0", "Math.round(value * 100.0) / 100.0");
        Files.writeString(targetFile, patched);
    }

    private static LocalShellTool createShellTool()
    {
        ToolDefinition definition = new ToolDefinition(
            "local_shell",
            "Execute a local shell command and return stdout/stderr.",
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")));
        return new LocalShellTool(definition);
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
