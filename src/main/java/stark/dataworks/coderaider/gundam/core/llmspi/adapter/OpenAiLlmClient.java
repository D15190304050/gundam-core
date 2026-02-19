package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

/**
 * OpenAI adapter built on the OpenAI-compatible base client.
 */
public class OpenAiLlmClient extends OpenAiCompatibleLlmClient
{
    public OpenAiLlmClient(String apiKey, String model)
    {
        super(OpenAiCompatibleConfiguration.openAi(apiKey, model));
    }
}
