package stark.dataworks.coderaider.gundam.core.llmspi;

/**
 * LlmStreamListener implements provider-agnostic model invocation contracts.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@FunctionalInterface
public interface LlmStreamListener
{

    /**
     * Performs on delta as part of LlmStreamListener runtime responsibilities.
     * @param delta The delta used by this operation.
     */
    void onDelta(String delta);
}

