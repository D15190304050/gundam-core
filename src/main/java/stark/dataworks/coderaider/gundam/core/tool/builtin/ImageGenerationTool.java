package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.IImageGenerator;
import stark.dataworks.coderaider.gundam.core.multimodal.ImageGenerationRequest;
import stark.dataworks.coderaider.gundam.core.oss.IOssClient;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * ImageGenerationTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class ImageGenerationTool extends AbstractBuiltinTool
{
    private final IImageGenerator imageGenerator;
    private final IOssClient ossClient;

    /**
     * Initializes ImageGenerationTool with required runtime dependencies and options.
     * @param definition definition object.
     */
    public ImageGenerationTool(ToolDefinition definition)
    {
        this(definition, null, null);
    }

    /**
     * Initializes ImageGenerationTool with required runtime dependencies and options.
     * @param definition definition object.
     * @param imageGenerator image generator.
     * @param ossClient oss client.
     */
    public ImageGenerationTool(ToolDefinition definition, IImageGenerator imageGenerator, IOssClient ossClient)
    {
        super(definition, ToolCategory.IMAGE_GENERATION);
        this.imageGenerator = imageGenerator;
        this.ossClient = ossClient;
    }

    /**
     * Executes this tool operation and returns the produced output.
     * @param Map<String map<string.
     * @param input input payload.
     * @return Tool execution output returned by the MCP server.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String prompt = Objects.toString(input.getOrDefault("prompt", ""), "");
        if (imageGenerator == null)
        {
            return "ImageGeneration(simulated): prompt=" + prompt;
        }

        ImageGenerationRequest request = new ImageGenerationRequest(
            prompt,
            Objects.toString(input.getOrDefault("model", ""), ""),
            Objects.toString(input.getOrDefault("size", ""), ""),
            Map.of());

        GeneratedAsset generatedAsset = imageGenerator.generate(request);
        String url = generatedAsset.getUri();

        if (ossClient != null && !url.isBlank())
        {
            String key = "generated/images/" + UUID.randomUUID() + ".bin";
            ossClient.putObject(key, url.getBytes(), generatedAsset.getMimeType());
            url = ossClient.getPublicUrl(key);
        }

        return "ImageGeneration: prompt=" + prompt + ", uri=" + url;
    }
}
