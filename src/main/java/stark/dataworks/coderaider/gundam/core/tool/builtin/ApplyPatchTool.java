package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.gundam.core.editor.DiffApplier;
import stark.dataworks.coderaider.gundam.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;

/**
 * ApplyPatchTool is a hosted tool that lets the model request file mutations via unified diffs.
 * The editor implementation handles the actual file operations.
 */
public class ApplyPatchTool implements ITool
{
    private static final String TOOL_NAME = "apply_patch";
    private static final String TOOL_DESCRIPTION = "Apply file operations using unified diffs. " +
        "Supported types: 'create_file' (or 'create'), 'update_file' (or 'update'), 'delete_file' (or 'delete'). " +
        "For create_file, provide 'content' or 'diff'. For update_file, provide 'diff'. For delete_file, only 'path' is needed.";

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
                new ToolParameterSchema("operation", "object", true,
                    "The patch operation. Must have 'type' (create_file/update_file/delete_file) and 'path'. " +
                    "For create_file, include 'content' or 'diff'. For update_file, include 'diff'.")
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
            return errorResult("Missing 'operation' parameter");
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
