package stark.dataworks.coderaider.gundam.core.multimodal;

/**
 * IAudioGenerator defines provider-agnostic audio generation contract.
 */
public interface IAudioGenerator
{
    GeneratedAsset generate(AudioGenerationRequest request);
}
