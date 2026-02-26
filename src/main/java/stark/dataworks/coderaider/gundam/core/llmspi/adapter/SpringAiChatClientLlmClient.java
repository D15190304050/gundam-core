package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import org.springframework.ai.chat.client.ChatClient;
import stark.dataworks.coderaider.gundam.core.llmspi.IMultimodalLlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.multimodal.AudioGenerationRequest;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.ImageGenerationRequest;
import stark.dataworks.coderaider.gundam.core.multimodal.VideoGenerationRequest;
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
public class SpringAiChatClientLlmClient implements IMultimodalLlmClient
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


    @Override
    public GeneratedAsset generate(ImageGenerationRequest request)
    {
        throw new UnsupportedOperationException("Image generation is not implemented for Spring AI ChatClient adapter.");
    }

    @Override
    public GeneratedAsset generate(VideoGenerationRequest request)
    {
        throw new UnsupportedOperationException("Video generation is not implemented for Spring AI ChatClient adapter.");
    }

    @Override
    public GeneratedAsset generate(AudioGenerationRequest request)
    {
        throw new UnsupportedOperationException("Audio generation is not implemented for Spring AI ChatClient adapter.");
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
