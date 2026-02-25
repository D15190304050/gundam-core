package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
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
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 9) Use local docx skill to analyze a Word file and write a markdown analysis output.
 *
 * Usage:
 * java Example09DocxSkillAnalysisStreaming [model] [apiKey] [prompt] [localSkillName]
 */
public class Example09DocxSkillAnalysisStreamingTest
{
    private static final int MAX_TOOL_OUTPUT_CHARS = 12000;

    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        String prompt = "Use the loaded docx skill end-to-end for "
            + "src/main/resources/DocsForAnalysis/162235007倪英杰一种分布式存储系统中的元数据管理技术研究与实现.docx. "
            + "Mandatory workflow: "
            + "(1) inspect the docx skill files and scripts with list_files/read_file; "
            + "(2) inspect the target docx and related analysis markdown; "
            + "(3) run shell tools from the skill (for example pandoc) to extract content; "
            + "(4) update src/main/resources/DocsForAnalysis/162235007倪英杰一种分布式存储系统中的元数据管理技术研究与实现-analysis.md "
            + "with comprehensive analysis and workflow notes. "
            + "Do not stop early. Complete all 4 steps before final response.";
        String localSkillName = "docx";
        Path workspaceRoot = Path.of("").toAbsolutePath().normalize();
        Path analysisPath = workspaceRoot.resolve("src/main/resources/DocsForAnalysis/162235007倪英杰一种分布式存储系统中的元数据管理技术研究与实现-analysis.md");
        String skillMarkdown = loadSkillMarkdown(localSkillName);

        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "MODEL_SCOPE_API_KEY is required for this integration test.");

        String beforeContent = readFileIfExists(analysisPath);
        FileTime beforeModifiedTime = readLastModifiedIfExists(analysisPath);

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

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(tools)
            .agentRegistry(registry)
            .eventPublisher(createConsoleStreamingPublisher())
            .build();

        RunConfiguration config = new RunConfiguration(24, null, 0.2, 4096, "auto", "text", Map.of(), new RetryPolicy(3, 1500));
        System.out.println("Workspace root: " + workspaceRoot);
        System.out.println("Loaded local skill: " + localSkillName);
        ContextResult result = runner.runStreamed(registry.get("docx-skills-agent").orElseThrow(), prompt, config, ExampleSupport.noopHooks());
        String finalOutput = result.getFinalOutput();
        System.out.println("\nFinal output: " + finalOutput);
        Assumptions.assumeFalse(finalOutput != null && finalOutput.startsWith("Run failed:"),
            "Skipping update assertions because model invocation failed: " + finalOutput);

        String afterContent = readFileIfExists(analysisPath);
        FileTime afterModifiedTime = readLastModifiedIfExists(analysisPath);
        Assertions.assertNotNull(afterContent, "analysis markdown must exist");
        boolean contentChanged = beforeContent == null || !beforeContent.equals(afterContent);
        boolean modifiedTimeChanged = beforeModifiedTime == null || !beforeModifiedTime.equals(afterModifiedTime);
        Assertions.assertTrue(contentChanged || modifiedTimeChanged, "analysis markdown was not updated");
    }

    private static String loadSkillMarkdown(String localSkillName)
    {
        String resourcePath = "skills/" + localSkillName + "/SKILL.md";
        try (InputStream inputStream = Example09DocxSkillAnalysisStreamingTest.class.getClassLoader().getResourceAsStream(resourcePath))
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
        return "You are a practical engineering assistant.\n"
            + "Use tools to inspect files, run shell commands, and update files when needed.\n"
            + "Follow the loaded skill definition exactly.\n"
            + "Never shortcut the workflow. Verify skill scripts and repository files before final answer.\n"
            + "Workspace root is: " + workspaceRoot + "\n\n"
            + "Loaded skill definition:\n"
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
                        new ToolParameterSchema("max_depth", "number", false, "Max recursion depth (default 2)"),
                        new ToolParameterSchema("max_entries", "number", false, "Max number of entries to return (default 200)")));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String rawPath = String.valueOf(input.getOrDefault("path", "."));
                    int maxDepth = readInt(input, "max_depth", 2, 0, 12);
                    int maxEntries = readInt(input, "max_entries", 200, 1, 2000);
                    Path target = resolveWorkspacePath(workspaceRoot, rawPath);
                    try (Stream<Path> stream = Files.walk(target, Math.max(0, Math.min(maxDepth, 8))))
                    {
                        return truncateToolOutput(stream
                            .map(path -> toWorkspacePath(workspaceRoot, path))
                            .limit(maxEntries)
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
                    List.of(
                        new ToolParameterSchema("path", "string", true, "Relative or absolute path inside workspace"),
                        new ToolParameterSchema("start_line", "number", false, "1-based start line (default 1)"),
                        new ToolParameterSchema("max_lines", "number", false, "Maximum lines to return (default 120)")));
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

                    int startLine = readInt(input, "start_line", 1, 1, Integer.MAX_VALUE);
                    int maxLines = readInt(input, "max_lines", 120, 1, 500);
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    if (lines.isEmpty())
                    {
                        return "file=" + toWorkspacePath(workspaceRoot, file) + "\n(empty file)";
                    }

                    int from = Math.min(startLine - 1, lines.size() - 1);
                    int to = Math.min(from + maxLines, lines.size());
                    StringBuilder output = new StringBuilder();
                    output.append("file=").append(toWorkspacePath(workspaceRoot, file)).append('\n');
                    output.append("lines=").append(from + 1).append('-').append(to).append(" of ").append(lines.size()).append('\n');
                    for (int lineIndex = from; lineIndex < to; lineIndex++)
                    {
                        output.append(lineIndex + 1).append(": ").append(lines.get(lineIndex)).append('\n');
                    }
                    if (to < lines.size())
                    {
                        output.append("...truncated...");
                    }
                    return truncateToolOutput(output.toString());
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
                    ProcessBuilder processBuilder = new ProcessBuilder()
                        .directory(workspaceRoot.toFile())
                        .redirectErrorStream(true);
                    
                    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
                    if (isWindows)
                    {
                        processBuilder.command("powershell.exe", "-NoProfile", "-Command", command);
                    }
                    else
                    {
                        processBuilder.command("bash", "-lc", command);
                    }
                    
                    Process process = processBuilder.start();
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

    private static int readInt(Map<String, Object> input, String key, int defaultValue, int min, int max)
    {
        Object raw = input.get(key);
        if (raw == null)
        {
            return defaultValue;
        }
        int parsed = Integer.parseInt(String.valueOf(raw));
        return Math.max(min, Math.min(max, parsed));
    }

    private static String readFileIfExists(Path path)
    {
        try
        {
            return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : null;
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Failed to read file: " + path, exception);
        }
    }

    private static FileTime readLastModifiedIfExists(Path path)
    {
        try
        {
            return Files.exists(path) ? Files.getLastModifiedTime(path) : null;
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Failed to read modified time: " + path, exception);
        }
    }
}
