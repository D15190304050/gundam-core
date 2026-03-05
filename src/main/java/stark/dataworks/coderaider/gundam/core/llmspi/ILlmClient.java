package stark.dataworks.coderaider.gundam.core.llmspi;

import stark.dataworks.coderaider.gundam.core.model.ToolCall;

/**
 * ILlmClient implements provider-agnostic model invocation contracts.
 */
public interface ILlmClient
{

    /**
     * Runs a non-streaming model call.
     * @param request request payload.
     * @return llm response result.
     */
    LlmResponse chat(LlmRequest request);

    /**
     * Streams model output for the supplied request.
     * @param request model/tool request payload.
     * @param listener event listener.
     * @return llm response result.
     */
    default LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
    {
        LlmResponse response = chat(request);

        if (listener != null && response != null)
        {
            if (!response.getContent().isBlank())
                listener.onDelta(response.getContent());

            for (ToolCall toolCall : response.getToolCalls())
                listener.onToolCall(toolCall);

            if (response.getTokenUsage() != null)
                listener.onTokenUsage(response.getTokenUsage());

            response.getHandoffAgentId().ifPresent(listener::onHandoff);
            listener.onCompleted(response);
        }

        return response;
    }
}
