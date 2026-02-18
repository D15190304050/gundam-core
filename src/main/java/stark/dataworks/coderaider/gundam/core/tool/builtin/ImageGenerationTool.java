package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class ImageGenerationTool.
 */

public class ImageGenerationTool extends AbstractBuiltinTool
{
    /**
     * Creates a new ImageGenerationTool instance.
     */
    public ImageGenerationTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.IMAGE_GENERATION);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        return "ImageGeneration(simulated): prompt=" + input.getOrDefault("prompt", "");
    }
}
