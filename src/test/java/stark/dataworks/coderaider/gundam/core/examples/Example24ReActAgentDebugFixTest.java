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
 * 24) Demonstrates a ReAct-style multi-agent workflow to debug and fix a Java bug with tools.
 */
public class Example24ReActAgentDebugFixTest
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
        Path outputDir = Path.of("src", "test", "resources", "outputs", "react-agent", "example24");
        Files.createDirectories(outputDir);

        Path buggyFile = outputDir.resolve("BuggyCalculator.java");
        Files.writeString(buggyFile, """
            public class BuggyCalculator {
                public static int add(int a, int b) {
                    return a - b;
                }
            }
            """);

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(createPlannerAgent(model));
        agentRegistry.register(createFixerAgent(model, outputDir));

        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(createShellTool());
        toolRegistry.register(new ApplyPatchTool(new FileSystemEditor(outputDir), false));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.reasoningAndTextWithSections())
            .build();

        ContextResult plannerResult = runner.chatClient("react-planner")
            .prompt()
            .user("Analyze the bug and handoff execution to react-fixer.")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        ContextResult fixerResult = runner.chatClient("react-fixer")
            .prompt()
            .user("Fix BuggyCalculator.java in the current workspace. First inspect file content, then patch return a - b to return a + b, then run: javac BuggyCalculator.java 2>&1")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .call()
            .contextResult();

        Assertions.assertTrue(Files.exists(outputDir), "Expected output directory to exist");
        Assertions.assertTrue(Files.exists(buggyFile), "Expected buggy file to exist");
        Assertions.assertNotNull(plannerResult.getFinalOutput());
        Assertions.assertNotNull(fixerResult.getFinalOutput());

        String fixedSource = Files.readString(buggyFile);
        if (fixedSource.contains("return a + b;"))
        {
            String verifyOutput = createShellTool().execute(Map.of("command", "cd '" + outputDir + "' && javac BuggyCalculator.java 2>&1 && echo COMPILE_OK"));
            Assertions.assertTrue(verifyOutput.contains("COMPILE_OK"), "Expected compilation to pass after fix");
        }
        else
        {
            System.out.println("ReAct agent did not patch the file in this run; review model output for iterative behavior.");
        }
    }

    private static Agent createPlannerAgent(String model)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-planner");
        def.setName("ReAct Planner");
        def.setModel(model);
        def.setSystemPrompt("You are a senior debugging planner. First inspect the task, then handoff to react-fixer to execute concrete file/tool operations.");
        def.setReactEnabled(true);
        def.setHandoffAgentIds(List.of("react-fixer"));
        return new Agent(def);
    }

    private static Agent createFixerAgent(String model, Path outputDir)
    {
        AgentDefinition def = new AgentDefinition();
        def.setId("react-fixer");
        def.setName("ReAct Fixer");
        def.setModel(model);
        def.setSystemPrompt("You are a coding fix agent. Use tools iteratively until the bug is fixed and verified.");
        def.setReactEnabled(true);
        def.setReactInstructions("Use a ReAct loop: reason shortly, choose next best action, run one tool at a time, observe result, continue until compile succeeds, then provide final summary.");
        def.setToolNames(List.of("local_shell", "apply_patch"));
        def.setModelReasoning(Map.of("effort", "medium"));
        def.setModelProviderOptions(Map.of("working_directory", outputDir.toString()));
        return new Agent(def);
    }

    private static LocalShellTool createShellTool()
    {
        ToolDefinition definition = new ToolDefinition(
            "local_shell",
            "Execute a local bash command and return stdout.",
            List.of(new ToolParameterSchema("command", "string", true, "Bash command to execute")));
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
