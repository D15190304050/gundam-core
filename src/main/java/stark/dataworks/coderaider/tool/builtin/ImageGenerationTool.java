package stark.dataworks.coderaider.tool.builtin;

import java.util.Map;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class ImageGenerationTool extends AbstractBuiltinTool {
    public ImageGenerationTool(ToolDefinition definition) {
        super(definition, ToolCategory.IMAGE_GENERATION);
    }

    @Override
    public String execute(Map<String, Object> input) {
        return "ImageGeneration(simulated): prompt=" + input.getOrDefault("prompt", "");
    }
}
