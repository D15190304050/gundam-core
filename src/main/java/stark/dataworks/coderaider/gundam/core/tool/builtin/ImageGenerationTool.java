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
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class ImageGenerationTool extends AbstractBuiltinTool
{
    private final IImageGenerator imageGenerator;
    private final IOssClient ossClient;

    /**
     * Performs image generation tool as part of ImageGenerationTool runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public ImageGenerationTool(ToolDefinition definition)
    {
        this(definition, null, null);
    }

    /**
     * Performs image generation tool as part of ImageGenerationTool runtime responsibilities.
     * @param definition The definition used by this operation.
     * @param imageGenerator The image generator used by this operation.
     * @param ossClient The oss client used by this operation.
     */
    public ImageGenerationTool(ToolDefinition definition, IImageGenerator imageGenerator, IOssClient ossClient)
    {
        super(definition, ToolCategory.IMAGE_GENERATION);
        this.imageGenerator = imageGenerator;
        this.ossClient = ossClient;
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
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
