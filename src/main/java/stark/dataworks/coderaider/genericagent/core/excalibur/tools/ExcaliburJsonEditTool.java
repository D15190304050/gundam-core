package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Trae-compatible JSON edit tool using the same public parameter names as Trae's json_edit_tool.
 */
public final class ExcaliburJsonEditTool extends AbstractBuiltinTool
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Path workspaceRoot;

    public ExcaliburJsonEditTool(Path workspaceRoot)
    {
        super(new ToolDefinition(
            "json_edit_tool",
            "Tool for editing JSON files with JSONPath expressions using Trae-compatible operation/file_path/json_path/value parameters.",
            List.of(
                new ToolParameterSchema("operation", "string", true, "One of: view, set, add, remove."),
                new ToolParameterSchema("file_path", "string", true, "Absolute JSON file path inside the workspace."),
                new ToolParameterSchema("json_path", "string", false, "JSONPath such as $, $.a.b, $.items[0]."),
                new ToolParameterSchema("value", "object", false, "Value to set or add."),
                new ToolParameterSchema("pretty_print", "boolean", false, "Whether to pretty-print JSON output; defaults to true."))),
            ToolCategory.FUNCTION);
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        try
        {
            String operation = String.valueOf(input.getOrDefault("operation", input.getOrDefault("command", "view"))).toLowerCase();
            Path filePath = requireWorkspacePath(input.get("file_path"));
            boolean prettyPrint = booleanValue(input.get("pretty_print"), true);
            JsonNode root = loadJson(filePath);
            String jsonPath = input.get("json_path") == null ? "$" : String.valueOf(input.get("json_path"));
            return switch (operation)
            {
                case "view" -> view(filePath, root, jsonPath, prettyPrint);
                case "set" -> set(filePath, root, jsonPath, input.get("value"), prettyPrint);
                case "add" -> add(filePath, root, jsonPath, input.get("value"), prettyPrint);
                case "remove" -> remove(filePath, root, jsonPath, prettyPrint);
                default -> error("Unknown operation: " + operation + ". Supported operations: view, set, add, remove");
            };
        }
        catch (Exception ex)
        {
            return error("JSON edit tool error: " + ex.getMessage());
        }
    }

    private JsonNode loadJson(Path filePath) throws IOException
    {
        if (!Files.exists(filePath))
        {
            throw new IOException("File does not exist: " + filePath);
        }
        String content = Files.readString(filePath, StandardCharsets.UTF_8).trim();
        if (content.isEmpty())
        {
            throw new IOException("File is empty: " + filePath);
        }
        return OBJECT_MAPPER.readTree(content);
    }

    private String view(Path filePath, JsonNode root, String jsonPath, boolean prettyPrint) throws IOException
    {
        JsonNode node = "$".equals(jsonPath) ? root : resolve(root, parse(jsonPath));
        if (node == null)
        {
            return success("No matches found for JSONPath: " + jsonPath);
        }
        String json = prettyPrint
            ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node)
            : OBJECT_MAPPER.writeValueAsString(node);
        return success(("$".equals(jsonPath) ? "JSON content of " + filePath : "JSONPath '" + jsonPath + "' matches:") + "\n" + json);
    }

    private String set(Path filePath, JsonNode root, String jsonPath, Object value, boolean prettyPrint) throws IOException
    {
        if (value == null)
        {
            return error("A 'value' parameter is required for the 'set' operation.");
        }
        if ("$".equals(jsonPath))
        {
            write(filePath, OBJECT_MAPPER.valueToTree(value), prettyPrint);
            return success("Successfully updated root JSON value");
        }
        List<PathToken> tokens = parse(jsonPath);
        JsonNode parent = resolve(root, tokens.subList(0, tokens.size() - 1));
        if (parent == null)
        {
            return error("No matches found for JSONPath: " + jsonPath);
        }
        setLeaf(parent, tokens.get(tokens.size() - 1), OBJECT_MAPPER.valueToTree(value));
        write(filePath, root, prettyPrint);
        return success("Successfully updated JSONPath '" + jsonPath + "' with value: " + OBJECT_MAPPER.writeValueAsString(value));
    }

    private String add(Path filePath, JsonNode root, String jsonPath, Object value, boolean prettyPrint) throws IOException
    {
        if (value == null)
        {
            return error("A 'value' parameter is required for the 'add' operation.");
        }
        List<PathToken> tokens = parse(jsonPath);
        if (tokens.isEmpty())
        {
            return error("json_path parameter is required and must be a string for the 'add' operation.");
        }
        JsonNode parent = ensureParent(root, tokens.subList(0, tokens.size() - 1));
        addLeaf(parent, tokens.get(tokens.size() - 1), OBJECT_MAPPER.valueToTree(value));
        write(filePath, root, prettyPrint);
        return success("Successfully added value at JSONPath '" + jsonPath + "'");
    }

    private String remove(Path filePath, JsonNode root, String jsonPath, boolean prettyPrint) throws IOException
    {
        if ("$".equals(jsonPath))
        {
            write(filePath, OBJECT_MAPPER.createObjectNode(), prettyPrint);
            return success("Successfully removed root JSON value");
        }
        List<PathToken> tokens = parse(jsonPath);
        JsonNode parent = resolve(root, tokens.subList(0, tokens.size() - 1));
        if (parent == null)
        {
            return error("No matches found for JSONPath: " + jsonPath);
        }
        removeLeaf(parent, tokens.get(tokens.size() - 1), jsonPath);
        write(filePath, root, prettyPrint);
        return success("Successfully removed value(s) at JSONPath '" + jsonPath + "'");
    }

    private void write(Path filePath, JsonNode root, boolean prettyPrint) throws IOException
    {
        String json = prettyPrint
            ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root)
            : OBJECT_MAPPER.writeValueAsString(root);
        Files.writeString(filePath, json + "\n", StandardCharsets.UTF_8);
    }

    private JsonNode ensureParent(JsonNode root, List<PathToken> parentTokens)
    {
        JsonNode current = root;
        for (PathToken token : parentTokens)
        {
            if (token.field() != null)
            {
                if (!(current instanceof ObjectNode objectNode))
                {
                    throw new IllegalArgumentException("Cannot add key to non-object at path: " + token.field());
                }
                JsonNode next = objectNode.get(token.field());
                if (next == null || next.isNull())
                {
                    next = token.index() == null ? OBJECT_MAPPER.createObjectNode() : OBJECT_MAPPER.createArrayNode();
                    objectNode.set(token.field(), next);
                }
                current = next;
            }
            if (token.index() != null)
            {
                if (!(current instanceof ArrayNode arrayNode))
                {
                    throw new IllegalArgumentException("Cannot add element to non-array at indexed path");
                }
                while (arrayNode.size() <= token.index())
                {
                    arrayNode.add(OBJECT_MAPPER.createObjectNode());
                }
                current = arrayNode.get(token.index());
            }
        }
        return current;
    }

    private JsonNode resolve(JsonNode root, List<PathToken> tokens)
    {
        JsonNode current = root;
        for (PathToken token : tokens)
        {
            if (current == null)
            {
                return null;
            }
            if (token.field() != null)
            {
                current = current.get(token.field());
            }
            if (current != null && token.index() != null)
            {
                current = current.get(token.index());
            }
        }
        return current;
    }

    private void setLeaf(JsonNode parent, PathToken token, JsonNode value)
    {
        if (token.index() != null)
        {
            if (!(parent instanceof ArrayNode arrayNode) || token.index() >= arrayNode.size())
            {
                throw new IllegalArgumentException("No matches found for indexed JSONPath target");
            }
            arrayNode.set(token.index(), value);
            return;
        }
        if (!(parent instanceof ObjectNode objectNode) || token.field() == null || !objectNode.has(token.field()))
        {
            throw new IllegalArgumentException("No matches found for JSONPath target");
        }
        objectNode.set(token.field(), value);
    }

    private void addLeaf(JsonNode parent, PathToken token, JsonNode value)
    {
        if (token.index() != null)
        {
            if (!(parent instanceof ArrayNode arrayNode))
            {
                throw new IllegalArgumentException("Cannot add element to non-array at path");
            }
            int index = token.index();
            if (index >= arrayNode.size())
            {
                arrayNode.add(value);
            }
            else
            {
                arrayNode.insert(index, value);
            }
            return;
        }
        if (!(parent instanceof ObjectNode objectNode) || token.field() == null)
        {
            throw new IllegalArgumentException("Cannot add key to non-object at path");
        }
        objectNode.set(token.field(), value);
    }

    private void removeLeaf(JsonNode parent, PathToken token, String jsonPath)
    {
        if (token.index() != null)
        {
            if (!(parent instanceof ArrayNode arrayNode) || token.index() >= arrayNode.size())
            {
                throw new IllegalArgumentException("No matches found for JSONPath: " + jsonPath);
            }
            arrayNode.remove(token.index());
            return;
        }
        if (!(parent instanceof ObjectNode objectNode) || token.field() == null || !objectNode.has(token.field()))
        {
            throw new IllegalArgumentException("No matches found for JSONPath: " + jsonPath);
        }
        objectNode.remove(token.field());
    }

    private Path requireWorkspacePath(Object rawPath)
    {
        if (!(rawPath instanceof String raw) || raw.isBlank())
        {
            throw new IllegalArgumentException("file_path parameter is required");
        }
        Path target = Path.of(raw).toAbsolutePath().normalize();
        if (!target.isAbsolute())
        {
            throw new IllegalArgumentException("File path must be absolute: " + raw);
        }
        if (!target.startsWith(workspaceRoot))
        {
            throw new IllegalArgumentException("file_path must stay inside workspace: " + workspaceRoot);
        }
        return target;
    }

    private static boolean booleanValue(Object value, boolean defaultValue)
    {
        if (value instanceof Boolean bool)
        {
            return bool;
        }
        if (value instanceof String text && !text.isBlank())
        {
            return Boolean.parseBoolean(text);
        }
        return defaultValue;
    }

    private static List<PathToken> parse(String jsonPath)
    {
        if (jsonPath == null || jsonPath.isBlank() || "$".equals(jsonPath))
        {
            return List.of();
        }
        if (!jsonPath.startsWith("$."))
        {
            throw new IllegalArgumentException("Only JSONPath values starting with $. are supported.");
        }
        String body = jsonPath.substring(2);
        String[] segments = body.split("\\.");
        List<PathToken> tokens = new ArrayList<>();
        for (String segment : segments)
        {
            int bracket = segment.indexOf('[');
            if (bracket < 0)
            {
                tokens.add(new PathToken(segment, null));
                continue;
            }
            String field = bracket == 0 ? null : segment.substring(0, bracket);
            int closeBracket = segment.indexOf(']', bracket);
            if (closeBracket < 0)
            {
                throw new IllegalArgumentException("Invalid JSONPath segment: " + segment);
            }
            Integer index = Integer.parseInt(segment.substring(bracket + 1, closeBracket));
            tokens.add(new PathToken(field, index));
        }
        return tokens;
    }

    private static String success(String output)
    {
        return output;
    }

    private static String error(String output)
    {
        return "Error: " + output;
    }

    private record PathToken(String field, Integer index)
    {
    }
}
