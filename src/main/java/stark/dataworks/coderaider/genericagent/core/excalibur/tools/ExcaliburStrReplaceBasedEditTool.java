package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trae-compatible text editing tool mirroring the public parameter contract of str_replace_based_edit_tool.
 */
public final class ExcaliburStrReplaceBasedEditTool extends AbstractBuiltinTool
{
    private static final int SNIPPET_LINES = 4;

    private final Path workspaceRoot;

    public ExcaliburStrReplaceBasedEditTool(Path workspaceRoot)
    {
        super(new ToolDefinition(
            "str_replace_based_edit_tool",
            "Custom editing tool for viewing, creating and editing files using Trae-compatible parameters.",
            List.of(
                new ToolParameterSchema("command", "string", true, "Allowed options are: view, create, str_replace, insert."),
                new ToolParameterSchema("file_text", "string", false, "Required content for the create command."),
                new ToolParameterSchema("insert_line", "integer", false, "Required line number for insert; insertion happens after this 1-based line."),
                new ToolParameterSchema("new_str", "string", false, "Replacement text for str_replace or inserted text for insert."),
                new ToolParameterSchema("old_str", "string", false, "Exact unique text to replace for str_replace."),
                new ToolParameterSchema("path", "string", true, "Absolute path to a file or directory inside the workspace."),
                new ToolParameterSchema("view_range", "array", false, "Optional [startLine, endLine] range for file views."))),
            ToolCategory.FUNCTION);
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        String command = stringValue(input.get("command"));
        if (command == null || command.isBlank())
        {
            return failure("No command provided for the str_replace_based_edit_tool");
        }
        try
        {
            Path target = requireWorkspacePath(firstNonBlank(input.get("path"), input.get("file_path")));
            return switch (command)
            {
                case "view" -> success(view(target, input));
                case "create" -> success(create(target, firstNonBlank(input.get("file_text"), input.get("new_str"))));
                case "str_replace" -> success(replace(target, stringValue(input.get("old_str")), stringValue(input.get("new_str"))));
                case "insert" -> success(insert(target, input.get("insert_line"), stringValue(input.get("new_str"))));
                default -> failure("Unrecognized command " + command + ". Allowed commands are: view, create, str_replace, insert");
            };
        }
        catch (Exception ex)
        {
            return failure(ex.getMessage());
        }
    }

    private String view(Path target, Map<String, Object> input) throws IOException
    {
        if (Files.isDirectory(target))
        {
            try (var stream = Files.walk(target, 2))
            {
                String listing = stream
                    .filter(path -> !path.equals(target))
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .sorted(Comparator.naturalOrder())
                    .map(path -> workspaceRoot.relativize(path.toAbsolutePath().normalize()).toString()
                        + (Files.isDirectory(path) ? "/" : ""))
                    .collect(Collectors.joining("\n"));
                return "Here's the files and directories up to 2 levels deep in " + target + ", excluding hidden items:\n" + listing;
            }
        }
        if (!Files.exists(target))
        {
            throw new IOException("The path " + target + " does not exist. Please provide a valid path.");
        }
        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
        int start = 1;
        int end = lines.size();
        Object viewRange = input.get("view_range");
        if (viewRange instanceof List<?> range && !range.isEmpty())
        {
            if (range.size() != 2)
            {
                throw new IOException("Invalid view_range. It should contain exactly two integers.");
            }
            start = intValue(range.get(0), 1);
            int rawEnd = intValue(range.get(1), lines.size());
            end = rawEnd == -1 ? lines.size() : rawEnd;
        }
        start = Math.max(1, start);
        end = Math.min(lines.size(), end);
        List<String> numbered = new ArrayList<>();
        for (int i = start; i <= end; i++)
        {
            numbered.add(String.format("%6d\t%s", i, lines.get(i - 1)));
        }
        return numbered.isEmpty() ? "" : String.join("\n", numbered);
    }

    private String create(Path target, Object fileTextObj) throws IOException
    {
        if (Files.exists(target))
        {
            throw new IOException("File already exists at: " + target + ". Cannot overwrite files using command `create`.");
        }
        String content = fileTextObj == null ? "" : String.valueOf(fileTextObj);
        Path parent = target.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }
        Files.writeString(target, content, StandardCharsets.UTF_8);
        return "The file " + target + " has been created.";
    }

    private String replace(Path target, String oldStr, String newStr) throws IOException
    {
        if (oldStr == null || oldStr.isEmpty())
        {
            throw new IOException("old_str is required for str_replace.");
        }
        String content = expandTabs(Files.readString(target, StandardCharsets.UTF_8));
        String oldExpanded = expandTabs(oldStr);
        String newExpanded = newStr == null ? "" : expandTabs(newStr);
        int first = content.indexOf(oldExpanded);
        if (first < 0)
        {
            throw new IOException("No replacement was performed, old_str `" + oldStr + "` did not appear verbatim in " + target + ".");
        }
        if (content.indexOf(oldExpanded, first + oldExpanded.length()) >= 0)
        {
            throw new IOException("No replacement was performed. Multiple occurrences of old_str `" + oldStr + "` were found. Please ensure it is unique");
        }
        String updated = content.replace(oldExpanded, newExpanded);
        Files.writeString(target, updated, StandardCharsets.UTF_8);
        int replacementLine = content.substring(0, first).split("\\R", -1).length;
        String snippet = snippet(updated, replacementLine, replacementLine + SNIPPET_LINES + newExpanded.split("\\R", -1).length);
        return "The file " + target + " has been edited.\n" + snippet + "\nReview the changes and make sure they are as expected. Edit the file again if necessary.";
    }

    private String insert(Path target, Object insertLineObj, String newStr) throws IOException
    {
        int insertLine = intValue(insertLineObj, -1);
        if (insertLine < 0)
        {
            throw new IOException("insert_line is required for insert.");
        }
        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
        if (insertLine > lines.size())
        {
            throw new IOException("Invalid insert_line parameter: " + insertLine + ".");
        }
        List<String> updated = new ArrayList<>(lines);
        List<String> insertedLines = List.of((newStr == null ? "" : newStr).split("\\R", -1));
        updated.addAll(insertLine, insertedLines);
        Files.write(target, updated, StandardCharsets.UTF_8);
        String snippet = snippet(String.join("\n", updated), Math.max(1, insertLine - SNIPPET_LINES + 1), insertLine + SNIPPET_LINES + insertedLines.size());
        return "The file " + target + " has been edited.\n" + snippet + "\nReview the changes and make sure they are as expected. Edit the file again if necessary.";
    }

    private String snippet(String content, int startLine, int endLine)
    {
        String[] lines = content.split("\\R", -1);
        int safeStart = Math.max(1, startLine);
        int safeEnd = Math.min(lines.length, endLine);
        List<String> numbered = new ArrayList<>();
        for (int i = safeStart; i <= safeEnd; i++)
        {
            numbered.add(String.format("%6d\t%s", i, lines[i - 1]));
        }
        return String.join("\n", numbered);
    }

    private Path requireWorkspacePath(Object rawPath)
    {
        if (!(rawPath instanceof String raw) || raw.isBlank())
        {
            throw new IllegalArgumentException("No path provided for the str_replace_based_edit_tool");
        }
        Path target = Path.of(raw).toAbsolutePath().normalize();
        if (!target.isAbsolute())
        {
            throw new IllegalArgumentException("The path " + raw + " is not an absolute path.");
        }
        if (!target.startsWith(workspaceRoot))
        {
            throw new IllegalArgumentException("path must stay inside workspace: " + workspaceRoot);
        }
        return target;
    }

    private static String expandTabs(String value)
    {
        return value == null ? null : value.replace("\t", "    ");
    }

    private static String stringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private static Object firstNonBlank(Object first, Object second)
    {
        if (first instanceof String text && !text.isBlank())
        {
            return first;
        }
        return second;
    }

    private static int intValue(Object value, int defaultValue)
    {
        if (value instanceof Number number)
        {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank())
        {
            return Integer.parseInt(text);
        }
        return defaultValue;
    }

    private static String success(String output)
    {
        return output == null ? "" : output;
    }

    private static String failure(String output)
    {
        return "Error: " + output;
    }
}
