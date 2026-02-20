package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

/**
 * ModelScope adapter built on the OpenAI-compatible base client.
 * <p>
 * ModelScope provides OpenAI-compatible API for various models including Qwen.
 * Example usage:
 * <pre>
 * ModelScopeLlmClient client = new ModelScopeLlmClient("ms-xxx", "Qwen/Qwen3-4B");
 * </pre>
 */
public class ModelScopeLlmClient extends OpenAiCompatibleLlmClient
{
    public ModelScopeLlmClient(String apiKey, String model)
    {
        super(OpenAiCompatibleConfiguration.modelScope(apiKey, model));
    }
}
