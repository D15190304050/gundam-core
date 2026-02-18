package stark.dataworks.coderaider.gundam.core.llmspi;

/**
 * ILlmClient implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public interface ILlmClient
{

    /**
     * Performs chat as part of ILlmClient runtime responsibilities.
     * @param request The request used by this operation.
     * @return The value produced by this operation.
     */
    LlmResponse chat(LlmRequest request);
}
