package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import org.springframework.ai.chat.client.ChatClient;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Spring AI ChatClient based adapter.
 * <p>
 * This keeps kernel contracts unchanged while allowing applications to plug in Spring AI configured clients.
 * Tool-call and handoff extraction is still normalized by {@link OpenAiCompatibleResponseConverter#parseHandoff(String, List)} conventions.
 */
public class SpringAiChatClientLlmClient implements ILlmClient
{
    private final ChatClient chatClient;

    public SpringAiChatClientLlmClient(ChatClient chatClient)
    {
        this.chatClient = chatClient;
    }

    @Override
    public LlmResponse chat(LlmRequest request)
    {
        try
        {
            Object promptSpec = chatClient.prompt();
            String system = request.getMessages().stream()
                .filter(m -> m.getRole() == Role.SYSTEM)
                .map(Message::getContent)
                .findFirst()
                .orElse("");
            String user = request.getMessages().stream()
                .filter(m -> m.getRole() == Role.USER)
                .reduce((a, b) -> b)
                .map(Message::getContent)
                .orElse("");

            if (!system.isBlank())
            {
                promptSpec = invoke(promptSpec, "system", system);
            }
            if (!user.isBlank())
            {
                promptSpec = invoke(promptSpec, "user", user);
            }

            Object callSpec = invoke(promptSpec, "call");
            Object content = invoke(callSpec, "content");
            String text = content == null ? "" : String.valueOf(content);
            String handoff = OpenAiCompatibleResponseConverter.parseHandoff(text, List.of());
            return new LlmResponse(text, List.of(), handoff, null, "stop", Map.of());
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to invoke Spring AI ChatClient", ex);
        }
    }

    @Override
    public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
    {
        // Spring AI streaming APIs vary by model/client implementation. This default bridge performs a normal call
        // then emits a single delta, while keeping ILlmClient contract intact.
        LlmResponse response = chat(request);
        if (listener != null)
        {
            if (!response.getContent().isBlank())
            {
                listener.onDelta(response.getContent());
            }
            response.getHandoffAgentId().ifPresent(listener::onHandoff);
            listener.onCompleted(response);
        }
        return response;
    }

    private static Object invoke(Object target, String method, Object arg) throws Exception
    {
        Method m = target.getClass().getMethod(method, arg.getClass());
        return m.invoke(target, arg);
    }

    private static Object invoke(Object target, String method) throws Exception
    {
        Method m = target.getClass().getMethod(method);
        return m.invoke(target);
    }
}
