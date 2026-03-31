package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import stark.dataworks.coderaider.genericagent.core.excalibur.ckg.CKGDatabase;
import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ExcaliburCKGTool extends AbstractBuiltinTool
{
    private static final List<String> CKG_COMMANDS = List.of("search_function", "search_class", "search_class_method");
    private final CKGDatabase database;

    public ExcaliburCKGTool()
    {
        super(new ToolDefinition(
            "ckg",
            "Query the code knowledge graph of a codebase. Supports search_function, search_class, and search_class_method commands.",
            List.of(
                new ToolParameterSchema("command", "string", true, "The command to run. Allowed values: " + CKG_COMMANDS),
                new ToolParameterSchema("path", "string", true, "The path to the codebase."),
                new ToolParameterSchema("identifier", "string", true, "The identifier of the function or class to search for."),
                new ToolParameterSchema("print_body", "boolean", false, "Whether to print the body of the function or class."))),
            ToolCategory.FUNCTION);
        this.database = new CKGDatabase();
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        String command = input.get("command") != null ? String.valueOf(input.get("command")) : null;
        String path = input.get("path") != null ? String.valueOf(input.get("path")) : null;
        String identifier = input.get("identifier") != null ? String.valueOf(input.get("identifier")) : null;
        boolean printBody = input.get("print_body") != null ? Boolean.parseBoolean(String.valueOf(input.get("print_body"))) : true;

        if (command == null)
        {
            return "Error: No command provided for ckg tool";
        }
        if (path == null)
        {
            return "Error: No path provided for ckg tool";
        }
        if (identifier == null)
        {
            return "Error: No identifier provided for ckg tool";
        }

        if (!CKG_COMMANDS.contains(command))
        {
            return "Error: Invalid command '" + command + "'. Allowed commands are: " + CKG_COMMANDS;
        }

        Path codebasePath = Path.of(path);
        if (!codebasePath.toFile().exists())
        {
            return "Error: Path does not exist: " + path;
        }

        return switch (command)
        {
            case "search_function" -> database.searchFunction(codebasePath, identifier, printBody);
            case "search_class" -> database.searchClass(codebasePath, identifier, printBody);
            case "search_class_method" -> database.searchClassMethod(codebasePath, identifier, printBody);
            default -> "Error: Unknown command: " + command;
        };
    }
}