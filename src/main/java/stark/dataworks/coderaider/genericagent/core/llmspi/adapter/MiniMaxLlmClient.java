package stark.dataworks.coderaider.genericagent.core.llmspi.adapter;

/**
 * MiniMax adapter using OpenAI-compatible endpoint.
 */
public class MiniMaxLlmClient extends OpenAiCompatibleLlmClient
{
    public MiniMaxLlmClient(String apiKey, String model)
    {
        super(OpenAiCompatibleConfiguration.miniMax(apiKey, model));
    }
}
