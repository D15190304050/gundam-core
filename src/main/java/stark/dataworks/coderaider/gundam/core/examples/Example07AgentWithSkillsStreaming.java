package stark.dataworks.coderaider.gundam.core.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * 7) How to load a local skill, expose file tools, and stream output.
 *
 * Usage:
 * java Example07AgentWithSkillsStreaming [model] [apiKey] [prompt] [localSkillName|skillId] [skillId]
 */
public class Example07AgentWithSkillsStreaming
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_TOOL_OUTPUT_CHARS = 12000;

    public static void main(String[] args)
    {
        String model = args.length > 0 ? args[0] : "Qwen/Qwen3-Coder-480B-A35B-Instruct";
        String apiKey = args.length > 1 ? args[1] : System.getenv("MODEL_SCOPE_API_KEY");
        String prompt = args.length > 2 ? args[2] : System.getenv("EXAMPLE07_PROMPT");
        if (prompt == null || prompt.isBlank())
        {
            prompt = "Use the loaded skill to analyze this repository and update the target document accordingly. "
                + "When writing a file, perform a single write_file call with complete final content.";
        }
        String localSkillName = resolveLocalSkillName(args);
        String skillId = resolveSkillId(args);
        Path workspaceRoot = Path.of("").toAbsolutePath().normalize();
        String skillMarkdown = loadSkillMarkdown(localSkillName);

        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            System.exit(1);
        }

        AgentDefinition def = new AgentDefinition();
        def.setId("skills-agent");
        def.setName("Skills Agent");
        def.setModel(model);
        def.setToolNames(List.of("list_files", "read_file", "write_file"));
        def.setSystemPrompt(buildSystemPrompt(workspaceRoot, skillMarkdown));

        if (skillId != null && !skillId.isBlank())
        {
            def.setModelSkills(List.of(Map.of(
                "type", "skill_reference",
                "skill_id", skillId)));
            System.out.println("Using remote skill reference: " + skillId);
        }
        else if (args.length > 3 && args[3].startsWith("skill_"))
        {
            System.out.println("No local skill argument provided. Falling back to default local skill: " + localSkillName);
        }

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(def));

        ToolRegistry tools = new ToolRegistry();
        tools.register(createListFilesTool(workspaceRoot));
        tools.register(createReadFileTool(workspaceRoot));
        tools.register(createWriteFileTool(workspaceRoot));

        AgentRunner runner = ExampleSupport.runnerWithPublisher(
            new ModelScopeLlmClient(apiKey, model),
            tools,
            registry,
            null,
            createConsoleStreamingPublisher());

        RunConfiguration config = new RunConfiguration(24, null, 0.2, 4096, "auto", "text", Map.of(), new RetryPolicy(3, 1500));
        System.out.println("Workspace root: " + workspaceRoot);
        System.out.println("Loaded local skill: " + localSkillName);
        RunResult result = runner.runStreamed(registry.get("skills-agent").orElseThrow(), prompt, config, ExampleSupport.noopHooks());
        System.out.println("\nFinal output: " + result.getFinalOutput());
    }

    private static String resolveLocalSkillName(String[] args)
    {
        String localSkillName = System.getenv("LOCAL_SKILL_NAME");
        if (localSkillName == null || localSkillName.isBlank())
        {
            localSkillName = "architecture-analyzer";
        }
        if (args.length > 3 && !args[3].startsWith("skill_"))
        {
            return args[3];
        }
        return localSkillName;
    }

    private static String resolveSkillId(String[] args)
    {
        if (args.length > 4 && !args[4].isBlank())
        {
            return args[4];
        }
        if (args.length > 3 && args[3].startsWith("skill_"))
        {
            return args[3];
        }
        return System.getenv("ANTHROPIC_SKILL_ID");
    }

    private static String loadSkillMarkdown(String localSkillName)
    {
        String resourcePath = "skills/" + localSkillName + "/SKILL.md";
        try (InputStream inputStream = Example07AgentWithSkillsStreaming.class.getClassLoader().getResourceAsStream(resourcePath))
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
            + "You can inspect and update files only through provided tools.\n"
            + "Follow the loaded skill definition exactly.\n"
            + "Workspace root is: " + workspaceRoot + "\n\n"
            + "Loaded skill definition:\n"
            + skillMarkdown;
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
                        new ToolParameterSchema("max_entries", "number", false, "Max number of entries to return (default 60)")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String rawPath = String.valueOf(input.getOrDefault("path", "."));
                    int maxDepth = readInt(input, "max_depth", 2, 0, 16);
                    int maxEntries = readInt(input, "max_entries", 60, 1, 2000);

                    Path target = resolveWorkspacePath(workspaceRoot, rawPath);
                    if (!Files.exists(target))
                    {
                        return "ERROR: path does not exist: " + target;
                    }

                    try (Stream<Path> stream = Files.walk(target, maxDepth))
                    {
                        List<Path> collected = stream
                            .filter(path -> !path.equals(target))
                            .sorted()
                            .limit((long) maxEntries + 1)
                            .toList();

                        boolean truncated = collected.size() > maxEntries;
                        int size = truncated ? maxEntries : collected.size();
                        StringBuilder output = new StringBuilder();
                        output.append("path=").append(toWorkspacePath(workspaceRoot, target)).append('\n');
                        for (int index = 0; index < size; index++)
                        {
                            Path path = collected.get(index);
                            String marker = Files.isDirectory(path) ? "/" : "";
                            output.append("- ").append(toWorkspacePath(workspaceRoot, path)).append(marker).append('\n');
                        }
                        if (truncated)
                        {
                            output.append("...truncated...");
                        }
                        return truncateToolOutput(output.toString());
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
                    "Read a UTF-8 text file from the workspace with optional line range.",
                    List.of(
                        new ToolParameterSchema("path", "string", true, "Relative or absolute path inside workspace"),
                        new ToolParameterSchema("start_line", "number", false, "1-based line number to start (default 1)"),
                        new ToolParameterSchema("max_lines", "number", false, "Maximum number of lines to return (default 80)")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String rawPath = String.valueOf(input.getOrDefault("path", ""));
                    if (rawPath.isBlank())
                    {
                        return "ERROR: path is required";
                    }

                    int startLine = readInt(input, "start_line", 1, 1, Integer.MAX_VALUE);
                    int maxLines = readInt(input, "max_lines", 80, 1, 400);
                    Path file = resolveWorkspacePath(workspaceRoot, rawPath);
                    if (!Files.isRegularFile(file))
                    {
                        return "ERROR: not a file: " + file;
                    }

                    List<String> allLines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    if (allLines.isEmpty())
                    {
                        return "file=" + toWorkspacePath(workspaceRoot, file) + "\n(empty file)";
                    }

                    int from = Math.min(startLine - 1, allLines.size() - 1);
                    int to = Math.min(from + maxLines, allLines.size());
                    StringBuilder output = new StringBuilder();
                    output.append("file=").append(toWorkspacePath(workspaceRoot, file)).append('\n');
                    output.append("lines=").append(from + 1).append('-').append(to).append(" of ").append(allLines.size()).append('\n');
                    for (int lineIndex = from; lineIndex < to; lineIndex++)
                    {
                        output.append(lineIndex + 1).append(": ").append(allLines.get(lineIndex)).append('\n');
                    }
                    if (to < allLines.size())
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
        Set<String> writtenPaths = new HashSet<>();
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
                        new ToolParameterSchema("content", "string", true, "Full file content to write"),
                        new ToolParameterSchema("preserve_existing_headings", "boolean", false,
                            "Set true to enforce the same top-level markdown headings as the current file"),
                        new ToolParameterSchema("expected_headings", "array", false,
                            "Optional list of expected top-level markdown headings, in order"),
                        new ToolParameterSchema("allow_truncate", "boolean", false,
                            "Set true only when intentionally replacing with a much smaller file")
                    ));
            }

            @Override
            public String execute(Map<String, Object> input)
            {
                try
                {
                    String rawPath = String.valueOf(input.getOrDefault("path", ""));
                    if (rawPath.isBlank())
                    {
                        return "ERROR: path is required";
                    }
                    if (!input.containsKey("content"))
                    {
                        return "ERROR: content is required";
                    }

                    Path file = resolveWorkspacePath(workspaceRoot, rawPath);
                    String fileKey = file.toAbsolutePath().normalize().toString().toLowerCase();
                    boolean defaultPreserveHeadings = Files.isRegularFile(file) && isMarkdownFile(file);
                    boolean preserveExistingHeadings = readBoolean(input, "preserve_existing_headings", defaultPreserveHeadings);
                    boolean allowTruncate = readBoolean(input, "allow_truncate", false);
                    if (writtenPaths.contains(fileKey))
                    {
                        return "IGNORED: repeated write for the same file is blocked in this run. "
                            + "Keep the previous content and continue.";
                    }
                    Path parent = file.getParent();
                    if (parent != null)
                    {
                        Files.createDirectories(parent);
                    }
                    String content = toText(input.get("content"));
                    List<String> expectedHeadings = readStringList(input.get("expected_headings"));
                    if (preserveExistingHeadings && expectedHeadings.isEmpty() && Files.isRegularFile(file))
                    {
                        expectedHeadings = extractTopLevelHeadings(Files.readString(file, StandardCharsets.UTF_8));
                    }
                    if (!expectedHeadings.isEmpty())
                    {
                        List<String> actualHeadings = extractTopLevelHeadings(content);
                        if (!actualHeadings.equals(expectedHeadings))
                        {
                            return "ERROR: top-level headings mismatch. expected="
                                + expectedHeadings + ", actual=" + actualHeadings;
                        }
                    }
                    if (Files.isRegularFile(file) && !allowTruncate)
                    {
                        long existingLength = Files.size(file);
                        long incomingLength = content.length();
                        long minExpectedLength = Math.max(256L, existingLength / 2);
                        if (existingLength > 0 && incomingLength < minExpectedLength)
                        {
                            return "ERROR: refusing possible truncation. Provide complete file content, or set allow_truncate=true for intentional shrinking.";
                        }
                    }
                    Files.writeString(file, content, StandardCharsets.UTF_8);
                    writtenPaths.add(fileKey);
                    return "WROTE " + content.length() + " chars to " + toWorkspacePath(workspaceRoot, file);
                }
                catch (Exception exception)
                {
                    return "ERROR: " + exception.getMessage();
                }
            }
        };
    }

    private static int readInt(Map<String, Object> input, String key, int fallback, int min, int max)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return fallback;
        }

        int parsed;
        try
        {
            parsed = value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
        }
        catch (Exception exception)
        {
            return fallback;
        }

        if (parsed < min)
        {
            return min;
        }
        if (parsed > max)
        {
            return max;
        }
        return parsed;
    }

    private static boolean readBoolean(Map<String, Object> input, String key, boolean fallback)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return fallback;
        }
        if (value instanceof Boolean bool)
        {
            return bool;
        }
        String normalized = String.valueOf(value).trim().toLowerCase();
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized))
        {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized))
        {
            return false;
        }
        return fallback;
    }

    private static List<String> readStringList(Object value)
    {
        if (value == null)
        {
            return List.of();
        }
        if (value instanceof List<?> list)
        {
            List<String> out = new ArrayList<>();
            for (Object item : list)
            {
                String text;
                if (item instanceof Map<?, ?> map && map.get("text") != null)
                {
                    text = String.valueOf(map.get("text")).trim();
                }
                else
                {
                    text = String.valueOf(item).trim();
                }
                if (!text.isBlank())
                {
                    out.add(text);
                }
            }
            return out;
        }

        String text = String.valueOf(value).trim();
        if (text.isBlank())
        {
            return List.of();
        }
        if (text.startsWith("[") && text.endsWith("]"))
        {
            try
            {
                List<?> parsed = OBJECT_MAPPER.readValue(text, List.class);
                return readStringList(parsed);
            }
            catch (Exception ignored)
            {
            }
        }

        List<String> out = new ArrayList<>();
        for (String part : Arrays.stream(text.split("[\\r\\n,]+")).map(String::trim).toList())
        {
            if (!part.isBlank())
            {
                out.add(part);
            }
        }
        return out;
    }

    private static List<String> extractTopLevelHeadings(String markdown)
    {
        if (markdown == null || markdown.isBlank())
        {
            return List.of();
        }
        List<String> headings = new ArrayList<>();
        for (String line : markdown.split("\\r?\\n"))
        {
            String trimmed = line.trim();
            if (trimmed.startsWith("## "))
            {
                String heading = trimmed.substring(3).trim();
                if (!heading.isBlank())
                {
                    headings.add(heading);
                }
            }
        }
        return headings;
    }

    private static boolean isMarkdownFile(Path file)
    {
        String name = file.getFileName() == null ? "" : file.getFileName().toString().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown");
    }

    private static Path resolveWorkspacePath(Path workspaceRoot, String rawPath)
    {
        Path candidate = rawPath == null || rawPath.isBlank()
            ? workspaceRoot
            : Path.of(rawPath);
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
            Path relative = workspaceRoot.relativize(normalized);
            return relative.toString().replace('\\', '/');
        }
        return normalized.toString();
    }

    private static String toText(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof String text)
        {
            return text;
        }
        try
        {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        }
        catch (Exception exception)
        {
            return String.valueOf(value);
        }
    }

    private static String truncateToolOutput(String value)
    {
        if (value == null || value.length() <= MAX_TOOL_OUTPUT_CHARS)
        {
            return value;
        }
        return value.substring(0, MAX_TOOL_OUTPUT_CHARS) + "\n...truncated-by-tool-output-limit...";
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
}
