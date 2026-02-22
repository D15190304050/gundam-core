package stark.dataworks.coderaider.gundam.core.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.policy.RetryPolicy;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 8) Use local docx skill to analyze a Word file and write a markdown analysis output.
 *
 * Usage:
 * java Example08DocxSkillAnalysisStreaming [model] [apiKey] [prompt] [localSkillName]
 */
public class Example08DocxSkillAnalysisStreaming
{
    private static final int MAX_TOOL_OUTPUT_CHARS = 12000;

    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-4B";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2
            ? args[2]
            : "Analyze src/main/resources/DocsForAnalysis/162235007倪英杰一种分布式存储系统中的元数据管理技术研究与实现.docx "
            + "using the loaded docx skill. Write analysis markdown to "
            + "src/main/resources/DocsForAnalysis/162235007倪英杰一种分布式存储系统中的元数据管理技术研究与实现-analysis.md.";
        String localSkillName = args.length > 3 ? args[3] : "docx";
        Path workspaceRoot = Path.of("").toAbsolutePath().normalize();
        String skillMarkdown = loadSkillMarkdown(localSkillName);

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition def = new AgentDefinition();
        def.setId("docx-skills-agent");
        def.setName("Docx Skills Agent");
        def.setModel(model);
        def.setToolNames(List.of("list_files", "read_file", "write_file", "run_shell"));
        def.setSystemPrompt(buildSystemPrompt(workspaceRoot, skillMarkdown));

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(def));

        ToolRegistry tools = new ToolRegistry();
        tools.register(createListFilesTool(workspaceRoot));
        tools.register(createReadFileTool(workspaceRoot));
        tools.register(createWriteFileTool(workspaceRoot));
        tools.register(createRunShellTool(workspaceRoot));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            new ModelScopeLlmClient(apiKey, model),
            tools,
            registry,
            null,
            createConsoleStreamingPublisher());

        RunConfiguration config = new RunConfiguration(24, null, 0.2, 4096, "auto", "text", Map.of(), new RetryPolicy(3, 1500));
        System.out.println("Workspace root: " + workspaceRoot);
        System.out.println("Loaded local skill: " + localSkillName);
        RunResult result = runner.runStreamed(registry.get("docx-skills-agent").orElseThrow(), prompt, config, ExampleSupport.noopHooks());
        System.out.println("\nFinal output: " + result.getFinalOutput());
    }

    private static String loadSkillMarkdown(String localSkillName)
    {
        String resourcePath = "skills/" + localSkillName + "/SKILL.md";
        try (InputStream inputStream = Example08DocxSkillAnalysisStreaming.class.getClassLoader().getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalStateException("Skill resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Failed to load skill resource: " + resourcePath, exception);
        }
    }

    private static String buildSystemPrompt(Path workspaceRoot, String skillMarkdown)
    {
        return "You are a practical engineering assistant.\\n"
            + "Use tools to inspect files, run shell commands, and update files when needed.\\n"
            + "Workspace root is: " + workspaceRoot + "\\n\\n"
            + "Loaded skill definition:\\n"
            + skillMarkdown;
    }

    private static RunEventPublisher createConsoleStreamingPublisher()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new IRunEventListener()
        {
            @Override
            public void onEvent(RunEvent event)
            {
                if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
                {
                    String delta = (String) event.getAttributes().get("delta");
                    if (delta != null)
                    {
                        System.out.print(delta);
                        System.out.flush();
                    }
                }
                else if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("\n[Tool call: " + tool + "]");
                }
                else if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
                {
                    String tool = (String) event.getAttributes().get("tool");
                    System.out.println("[Tool completed: " + tool + "]");
                    System.out.print("Continuing stream: ");
                }
            }
        });
        return publisher;
    }

    private static ITool createListFilesTool(Path workspaceRoot)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "list_files",
                    "List files and directories within the workspace.",
                    List.of(
                        new ToolParameterSchema("path", "string", true, "Relative or absolute path inside workspace"),
                        new ToolParameterSchema("max_depth", "number", false, "Max recursion depth (default 2)")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String rawPath = String.valueOf(input.getOrDefault("path", "."));
                    int maxDepth = Integer.parseInt(String.valueOf(input.getOrDefault("max_depth", 2)));
                    Path target = resolveWorkspacePath(workspaceRoot, rawPath);
                    try (Stream<Path> stream = Files.walk(target, Math.max(0, Math.min(maxDepth, 8))))
                    {
                        return truncateToolOutput(stream
                            .map(path -> toWorkspacePath(workspaceRoot, path))
                            .limit(300)
                            .reduce((a, b) -> a + "\n" + b)
                            .orElse("(empty)"));
                    }
                }
                catch (Exception exception)
                {
                    return "ERROR: " + exception.getMessage();
                }
            }
        };
    }

    private static ITool createReadFileTool(Path workspaceRoot)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "read_file",
                    "Read a UTF-8 text file from the workspace.",
                    List.of(new ToolParameterSchema("path", "string", true, "Relative or absolute path inside workspace")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    Path file = resolveWorkspacePath(workspaceRoot, String.valueOf(input.getOrDefault("path", "")));
                    if (!Files.isRegularFile(file))
                    {
                        return "ERROR: not a file: " + file;
                    }
                    return truncateToolOutput(Files.readString(file, StandardCharsets.UTF_8));
                }
                catch (Exception exception)
                {
                    return "ERROR: " + exception.getMessage();
                }
            }
        };
    }

    private static ITool createWriteFileTool(Path workspaceRoot)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "write_file",
                    "Write UTF-8 text content to a file in the workspace.",
                    List.of(
                        new ToolParameterSchema("path", "string", true, "Relative or absolute path inside workspace"),
                        new ToolParameterSchema("content", "string", true, "Full file content to write")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    Path file = resolveWorkspacePath(workspaceRoot, String.valueOf(input.getOrDefault("path", "")));
                    Path parent = file.getParent();
                    if (parent != null)
                    {
                        Files.createDirectories(parent);
                    }
                    String content = String.valueOf(input.getOrDefault("content", ""));
                    Files.writeString(file, content, StandardCharsets.UTF_8);
                    return "WROTE " + content.length() + " chars to " + toWorkspacePath(workspaceRoot, file);
                }
                catch (Exception exception)
                {
                    return "ERROR: " + exception.getMessage();
                }
            }
        };
    }

    private static ITool createRunShellTool(Path workspaceRoot)
    {
        return new ITool()
        {
            @Override
            public ToolDefinition definition()
            {
                return new ToolDefinition(
                    "run_shell",
                    "Run a shell command in workspace and return stdout/stderr.",
                    List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String command = String.valueOf(input.getOrDefault("command", ""));
                    Process process = new ProcessBuilder("bash", "-lc", command)
                        .directory(workspaceRoot.toFile())
                        .redirectErrorStream(true)
                        .start();
                    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    int exitCode = process.waitFor();
                    return truncateToolOutput("exit=" + exitCode + "\n" + output);
                }
                catch (Exception exception)
                {
                    return "ERROR: " + exception.getMessage();
                }
            }
        };
    }

    private static Path resolveWorkspacePath(Path workspaceRoot, String rawPath)
    {
        Path candidate = rawPath == null || rawPath.isBlank() ? workspaceRoot : Path.of(rawPath);
        if (!candidate.isAbsolute())
        {
            candidate = workspaceRoot.resolve(candidate);
        }
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(workspaceRoot))
        {
            throw new IllegalArgumentException("Path escapes workspace root: " + rawPath);
        }
        return normalized;
    }

    private static String toWorkspacePath(Path workspaceRoot, Path path)
    {
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.startsWith(workspaceRoot))
        {
            return workspaceRoot.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString();
    }

    private static String truncateToolOutput(String value)
    {
        if (value == null || value.length() <= MAX_TOOL_OUTPUT_CHARS)
        {
            return value;
        }
        return value.substring(0, MAX_TOOL_OUTPUT_CHARS) + "\n...truncated-by-tool-output-limit...";
    }
}
