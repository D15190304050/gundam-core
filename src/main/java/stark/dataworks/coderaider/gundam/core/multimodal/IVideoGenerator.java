package stark.dataworks.coderaider.gundam.core.multimodal;

/**
 * IVideoGenerator defines provider-agnostic video generation contract.
 */
public interface IVideoGenerator
{
    GeneratedAsset generate(VideoGenerationRequest request);
}
