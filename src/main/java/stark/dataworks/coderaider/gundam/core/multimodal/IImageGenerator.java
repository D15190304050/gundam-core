package stark.dataworks.coderaider.gundam.core.multimodal;

/**
 * IImageGenerator defines provider-agnostic image generation contract.
 */
public interface IImageGenerator
{
    GeneratedAsset generate(ImageGenerationRequest request);
}
