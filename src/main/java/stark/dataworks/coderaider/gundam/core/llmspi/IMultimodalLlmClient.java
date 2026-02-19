package stark.dataworks.coderaider.gundam.core.llmspi;

import stark.dataworks.coderaider.gundam.core.multimodal.IAudioGenerator;
import stark.dataworks.coderaider.gundam.core.multimodal.IImageGenerator;
import stark.dataworks.coderaider.gundam.core.multimodal.IVideoGenerator;

/**
 * IMultimodalLlmClient extends text chat contracts with provider-side media generation capabilities.
 */
public interface IMultimodalLlmClient extends ILlmClient, IImageGenerator, IVideoGenerator, IAudioGenerator
{
}
