package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

/**
 * Qwen adapter using DashScope OpenAI-compatible endpoint.
 */
public class QwenLlmClient extends OpenAiCompatibleLlmClient
{
    public QwenLlmClient(String apiKey, String model)
    {
        super(OpenAiCompatibleConfiguration.qwen(apiKey, model));
    }
}
