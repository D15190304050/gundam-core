package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight Trae-compatible sequential thinking tool.
 */
public final class ExcaliburSequentialThinkingTool extends AbstractBuiltinTool
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ExcaliburSequentialThinkingTool()
    {
        super(new ToolDefinition(
            "sequentialthinking",
            "Record a structured reasoning step using Trae-compatible sequential thinking fields.",
            List.of(
                new ToolParameterSchema("thought", "string", true, "Current thinking step."),
                new ToolParameterSchema("thought_number", "integer", false, "Current thought number."),
                new ToolParameterSchema("total_thoughts", "integer", false, "Estimated total thoughts."),
                new ToolParameterSchema("next_thought_needed", "boolean", false, "Whether more thoughts are needed."),
                new ToolParameterSchema("is_revision", "boolean", false, "Whether this revises a previous thought."),
                new ToolParameterSchema("revises_thought", "integer", false, "Revised thought number."),
                new ToolParameterSchema("branch_from_thought", "integer", false, "Thought to branch from."),
                new ToolParameterSchema("branch_id", "string", false, "Alternative branch identifier."))),
            ToolCategory.FUNCTION);
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("thought", input.getOrDefault("thought", ""));
        response.put("thought_number", input.getOrDefault("thought_number", 1));
        response.put("total_thoughts", input.getOrDefault("total_thoughts", 1));
        response.put("next_thought_needed", input.getOrDefault("next_thought_needed", false));
        if (input.containsKey("is_revision"))
        {
            response.put("is_revision", input.get("is_revision"));
        }
        if (input.containsKey("revises_thought"))
        {
            response.put("revises_thought", input.get("revises_thought"));
        }
        if (input.containsKey("branch_from_thought"))
        {
            response.put("branch_from_thought", input.get("branch_from_thought"));
        }
        if (input.containsKey("branch_id"))
        {
            response.put("branch_id", input.get("branch_id"));
        }
        response.put("status", "recorded");
        try
        {
            return OBJECT_MAPPER.writeValueAsString(response);
        }
        catch (Exception ex)
        {
            return "{\"status\":\"failed\",\"output\":\"" + ex.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }
}
