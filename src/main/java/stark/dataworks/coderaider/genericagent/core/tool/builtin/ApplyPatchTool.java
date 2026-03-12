package stark.dataworks.coderaider.genericagent.core.tool.builtin;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.genericagent.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.genericagent.core.editor.DiffApplier;
import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.tool.ITool;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;

/**
 * ApplyPatchTool is a hosted tool that lets the model request file mutations via unified diffs.
 * The editor implementation handles the actual file operations.
 */
public class ApplyPatchTool implements ITool
{
    private static final String TOOL_NAME = "apply_patch";
    private static final String TOOL_DESCRIPTION = "Apply file operations using patch-style operations. " +
        "Accepted payloads: {type,path,diff|content} or {operation:{type,path,diff|content}}. " +
        "Supported types: 'create_file' (or 'create'), 'update_file' (or 'update'), 'delete_file' (or 'delete').";

    private final IApplyPatchEditor editor;
    private final boolean needsApproval;
    private final ToolDefinition definition;

    public ApplyPatchTool(IApplyPatchEditor editor)
    {
        this(editor, false);
    }

    public ApplyPatchTool(IApplyPatchEditor editor, boolean needsApproval)
    {
        this.editor = editor;
        this.needsApproval = needsApproval;
        this.definition = createDefinition();
    }

    private ToolDefinition createDefinition()
    {
        return new ToolDefinition(
            TOOL_NAME,
            TOOL_DESCRIPTION,
            List.of(
                new ToolParameterSchema("type", "string", false,
                    "Operation type: create_file, update_file, delete_file (aliases: create/update/delete)."),
                new ToolParameterSchema("path", "string", false, "Target file path, relative to workspace when possible."),
                new ToolParameterSchema("diff", "string", false, "Patch diff content for create/update operations."),
                new ToolParameterSchema("content", "string", false, "Raw file content for create_file when diff is omitted."),
                new ToolParameterSchema("operation", "object", false,
                    "Nested operation object alternative: {type, path, diff|content}."),
                new ToolParameterSchema("raw", "string", false,
                    "Raw JSON fallback when the model emits a serialized tool payload.")
            )
        );
    }

    @Override
    public ToolDefinition definition()
    {
        return definition;
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        Object operationObj = input.get("operation");
        if (operationObj == null)
        {
            operationObj = tryExtractOperation(input);
        }

        if (operationObj == null)
        {
            return errorResult("Missing patch payload. Provide either {\"type\":\"update_file\",\"path\":\"...\",\"diff\":\"...\"} " +
                "or {\"operation\":{\"type\":\"update_file\",\"path\":\"...\",\"diff\":\"...\"}}.");
        }

        if (!(operationObj instanceof Map))
        {
            return errorResult("'operation' must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> operation = (Map<String, Object>) operationObj;

        String typeStr = getStringValue(operation, "type");
        if (typeStr == null)
        {
            return errorResult("Missing 'type' in operation. Use: create_file, update_file, or delete_file");
        }

        String path = getStringValue(operation, "path");
        if (path == null || path.isEmpty())
        {
            return errorResult("Missing or empty 'path' in operation");
        }

        String diff = getStringValue(operation, "diff");
        String content = getStringValue(operation, "content");

        ApplyPatchOperation patchOp;
        ApplyPatchResult result;

        try
        {
            String normalizedType = normalizeOperationType(typeStr);

            switch (normalizedType)
            {
                case "create_file":
                    String createDiff = diff;
                    if ((createDiff == null || createDiff.isEmpty()) && content != null && !content.isEmpty())
                    {
                        createDiff = buildCreateDiff(path, content);
                    }
                    if (createDiff == null || createDiff.isEmpty())
                    {
                        return errorResult("Missing 'content' or 'diff' for create_file operation");
                    }
                    patchOp = ApplyPatchOperation.createFile(path, createDiff);
                    result = editor.createFile(patchOp);
                    break;

                case "update_file":
                    if (diff == null || diff.isEmpty())
                    {
                        return errorResult("Missing 'diff' for update_file operation");
                    }
                    patchOp = ApplyPatchOperation.updateFile(path, diff);
                    result = editor.updateFile(patchOp);
                    break;

                case "delete_file":
                    patchOp = ApplyPatchOperation.deleteFile(path);
                    result = editor.deleteFile(patchOp);
                    break;

                default:
                    return errorResult("Unknown operation type: " + typeStr + ". Use: create_file, update_file, or delete_file");
            }

            if (result == null)
            {
                return successResult("Operation completed");
            }

            return result.getStatus() == ApplyPatchResult.Status.COMPLETED
                ? successResult(result.getOutput())
                : errorResult(result.getOutput());
        }
        catch (Exception e)
        {
            return errorResult("Exception during patch operation: " + e.getMessage());
        }
    }

    private String normalizeOperationType(String type)
    {
        String lower = type.toLowerCase().trim();
        switch (lower)
        {
            case "create":
            case "create_file":
            case "add":
            case "new":
            case "write":
                return "create_file";

            case "update":
            case "update_file":
            case "modify":
            case "edit":
            case "patch":
                return "update_file";

            case "delete":
            case "delete_file":
            case "remove":
            case "rm":
                return "delete_file";

            default:
                return lower;
        }
    }

    private String buildCreateDiff(String path, String content)
    {
        StringBuilder diff = new StringBuilder();
        diff.append("diff --git a/").append(path).append(" b/").append(path).append("\n");
        diff.append("new file mode 100644\n");
        diff.append("index 0000000..0000000\n");
        diff.append("--- /dev/null\n");
        diff.append("+++ b/").append(path).append("\n");

        String[] lines = content.split("\n", -1);
        diff.append("@@ -0,0 +1,").append(lines.length).append(" @@\n");
        for (String line : lines)
        {
            diff.append("+").append(line).append("\n");
        }

        return diff.toString();
    }

    public boolean needsApproval()
    {
        return needsApproval;
    }

    public IApplyPatchEditor getEditor()
    {
        return editor;
    }

    private String getStringValue(Map<String, Object> map, String key)
    {
        Object value = map.get(key);
        if (value == null)
        {
            return null;
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> tryExtractOperation(Map<String, Object> input)
    {
        if (input.containsKey("type") && input.containsKey("path"))
        {
            return input;
        }

        Object rawObj = input.get("raw");
        if (rawObj != null)
        {
            Map<String, Object> op = tryParseRawAsOperation(rawObj.toString());
            if (op != null)
            {
                return op;
            }
        }

        for (Object value : input.values())
        {
            if (value instanceof Map)
            {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                if (mapValue.containsKey("type") && mapValue.containsKey("path"))
                {
                    return mapValue;
                }
            }
        }

        for (Object value : input.values())
        {
            if (value instanceof String)
            {
                Map<String, Object> op = tryParseRawAsOperation(value.toString());
                if (op != null)
                {
                    return op;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> tryParseRawAsOperation(String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return null;
        }

        try
        {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonToParse = raw.trim();

            for (int attempt = 0; attempt < 10; attempt++)
            {
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(jsonToParse);
                Map<String, Object> operation = extractOperationFromNode(mapper, node);
                if (operation != null)
                {
                    return operation;
                }

                if (node.isTextual())
                {
                    jsonToParse = node.asText();
                    continue;
                }

                break;
            }

            return tryExtractOperationBySubstring(mapper, jsonToParse);
        }
        catch (Exception e)
        {
            return tryExtractOperationBySubstring(new com.fasterxml.jackson.databind.ObjectMapper(), raw);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractOperationFromNode(com.fasterxml.jackson.databind.ObjectMapper mapper,
                                                         com.fasterxml.jackson.databind.JsonNode node)
    {
        if (node == null || !node.isObject())
        {
            return null;
        }

        if (node.has("operation"))
        {
            com.fasterxml.jackson.databind.JsonNode opNode = node.get("operation");
            if (opNode != null && opNode.has("type") && opNode.has("path"))
            {
                return mapper.convertValue(opNode, Map.class);
            }
        }

        if (node.has("type") && node.has("path"))
        {
            return mapper.convertValue(node, Map.class);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> tryExtractOperationBySubstring(com.fasterxml.jackson.databind.ObjectMapper mapper,
                                                               String raw)
    {
        if (raw == null)
        {
            return null;
        }

        int searchFrom = 0;
        while (searchFrom < raw.length())
        {
            int operationIndex = raw.indexOf("\"operation\"", searchFrom);
            if (operationIndex < 0)
            {
                break;
            }

            int objectStart = raw.indexOf('{', operationIndex);
            if (objectStart < 0)
            {
                break;
            }

            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            int objectEnd = -1;

            for (int i = objectStart; i < raw.length(); i++)
            {
                char ch = raw.charAt(i);
                if (escape)
                {
                    escape = false;
                    continue;
                }
                if (ch == '\\' && inString)
                {
                    escape = true;
                    continue;
                }
                if (ch == '"')
                {
                    inString = !inString;
                    continue;
                }
                if (inString)
                {
                    continue;
                }
                if (ch == '{')
                {
                    depth++;
                }
                else if (ch == '}')
                {
                    depth--;
                    if (depth == 0)
                    {
                        objectEnd = i;
                        break;
                    }
                }
            }

            if (objectEnd > 0)
            {
                String candidate = raw.substring(objectStart, objectEnd + 1);
                try
                {
                    com.fasterxml.jackson.databind.JsonNode opNode = mapper.readTree(candidate);
                    if (opNode.has("type") && opNode.has("path"))
                    {
                        return mapper.convertValue(opNode, Map.class);
                    }
                }
                catch (Exception ignored)
                {
                }
            }

            searchFrom = operationIndex + 10;
        }

        return null;
    }

    private String successResult(String message)
    {
        return "{\"status\": \"completed\", \"message\": " + toJsonString(message != null ? message : "OK") + "}";
    }

    private String errorResult(String error)
    {
        return "{\"status\": \"failed\", \"error\": " + toJsonString(error) + "}";
    }

    private String toJsonString(String value)
    {
        if (value == null)
        {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    public static String applyDiff(String original, String diff)
    {
        return DiffApplier.applyDiff(original, diff);
    }

    public static String applyCreateDiff(String diff)
    {
        return DiffApplier.applyDiff("", diff, DiffApplier.ApplyDiffMode.CREATE);
    }
}
