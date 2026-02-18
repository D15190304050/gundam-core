package stark.dataworks.coderaider.gundam.core.llmspi;
/**
 * Interface ILlmClient.
 */

public interface ILlmClient
{
    /**
     * Executes chat.
     */
    LlmResponse chat(LlmRequest request);
}
