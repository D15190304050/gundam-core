package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolSchemaJson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic adapter for OpenAI-compatible chat-completions endpoints.
 * <p>
 * Notes:
 * - Works with OpenAI, Gemini OpenAI-compatible API, Qwen, DeepSeek, Seed by changing base URL + key.
 * - Converts native tool-call payloads to {@link ToolCall}.
 * - Converts handoff markers from content/tool-call conventions into {@code handoffAgentId}.
 */
public class OpenAiCompatibleLlmClient implements ILlmClient
{
    private final OpenAiCompatibleConfiguration configuration;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(OpenAiCompatibleConfiguration configuration)
    {
        this.configuration = configuration;
        this.httpClient = HttpClient.newBuilder().connectTimeout(configuration.getTimeout()).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse chat(LlmRequest request)
    {
        try
        {
            String payload = objectMapper.writeValueAsString(toPayload(request, false));
            HttpRequest httpRequest = buildHttpRequest(payload);
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response.statusCode(), response.body());
            return OpenAiCompatibleResponseConverter.fromChatResponse(objectMapper, response.body());
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to call provider " + configuration.getProvider(), ex);
        }
    }

    @Override
    public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
    {
        try
        {
            String payload = objectMapper.writeValueAsString(toPayload(request, true));
            HttpRequest httpRequest = buildHttpRequest(payload);
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response.statusCode(), response.body());
            return consumeSse(response.body(), listener);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to stream from provider " + configuration.getProvider(), ex);
        }
    }

    private HttpRequest buildHttpRequest(String payload)
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(configuration.getBaseUrl() + "/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + configuration.getApiKey())
            .timeout(configuration.getTimeout())
            .POST(HttpRequest.BodyPublishers.ofString(payload));

        for (Map.Entry<String, String> entry : configuration.getHeaders().entrySet())
        {
            builder.header(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    private LlmResponse consumeSse(String body, ILlmStreamListener listener) throws IOException
    {
        StringBuilder content = new StringBuilder();
        String finishReason = "";
        List<ToolDelta> toolDeltas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(body)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (!line.startsWith("data:"))
                {
                    continue;
                }
                String data = line.substring("data:".length()).trim();
                if ("[DONE]".equals(data) || data.isBlank())
                {
                    continue;
                }

                JsonNode chunk = objectMapper.readTree(data);
                JsonNode choices = chunk.path("choices");
                if (!choices.isArray() || choices.isEmpty())
                {
                    continue;
                }

                JsonNode choice0 = choices.get(0);
                JsonNode delta = choice0.path("delta");
                String deltaText = OpenAiCompatibleResponseConverter.text(delta.path("content"));
                if (!deltaText.isBlank())
                {
                    content.append(deltaText);
                    if (listener != null)
                    {
                        listener.onDelta(deltaText);
                    }
                }

                JsonNode toolCalls = delta.path("tool_calls");
                if (toolCalls.isArray())
                {
                    for (JsonNode tc : toolCalls)
                    {
                        int idx = tc.path("index").asInt(toolDeltas.size());
                        while (toolDeltas.size() <= idx)
                        {
                            toolDeltas.add(new ToolDelta());
                        }
                        ToolDelta d = toolDeltas.get(idx);
                        String name = OpenAiCompatibleResponseConverter.text(tc.path("function").path("name"));
                        String argsPart = OpenAiCompatibleResponseConverter.text(tc.path("function").path("arguments"));
                        if (!name.isBlank())
                        {
                            d.name = name;
                        }
                        if (!argsPart.isBlank())
                        {
                            d.arguments.append(argsPart);
                        }
                    }
                }

                String fr = OpenAiCompatibleResponseConverter.text(choice0.path("finish_reason"));
                if (!fr.isBlank())
                {
                    finishReason = fr;
                }
            }
        }

        List<ToolCall> calls = new ArrayList<>();
        for (ToolDelta d : toolDeltas)
        {
            if (d.name == null || d.name.isBlank())
            {
                continue;
            }
            Map<String, Object> args = new HashMap<>();
            if (!d.arguments.isEmpty())
            {
                try
                {
                    JsonNode argsNode = objectMapper.readTree(d.arguments.toString());
                    if (argsNode.isObject())
                    {
                        args = objectMapper.convertValue(argsNode, Map.class);
                    }
                }
                catch (Exception ex)
                {
                    args.put("raw", d.arguments.toString());
                }
            }

            ToolCall call = new ToolCall(d.name, args);
            calls.add(call);
            if (listener != null)
            {
                listener.onToolCall(call);
            }
        }

        String handoff = OpenAiCompatibleResponseConverter.parseHandoff(content.toString(), calls);
        LlmResponse finalResponse = new LlmResponse(content.toString(), calls, handoff, null, finishReason, Map.of());
        if (listener != null)
        {
            if (handoff != null)
            {
                listener.onHandoff(handoff);
            }
            listener.onCompleted(finalResponse);
        }
        return finalResponse;
    }

    private Map<String, Object> toPayload(LlmRequest request, boolean stream)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", request.getModel());
        payload.put("messages", request.getMessages().stream().map(this::toMessage).toList());
        payload.put("temperature", request.getOptions().getTemperature());
        payload.put("max_tokens", request.getOptions().getMaxTokens());
        payload.put("tool_choice", request.getOptions().getToolChoice());
        payload.put("stream", stream);

        if (!request.getTools().isEmpty())
        {
            payload.put("tools", request.getTools().stream().map(this::toTool).toList());
        }

        if (!"text".equalsIgnoreCase(request.getOptions().getResponseFormat()))
        {
            payload.put("response_format", Map.of("type", request.getOptions().getResponseFormat()));
        }

        payload.putAll(request.getOptions().getProviderOptions());
        return payload;
    }

    private Map<String, Object> toMessage(Message message)
    {
        String role = switch (message.getRole())
        {
            case SYSTEM -> "system";
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case TOOL -> "tool";
        };

        Map<String, Object> out = new HashMap<>();
        out.put("role", role);
        out.put("content", message.getContent());
        return out;
    }

    private Map<String, Object> toTool(ToolDefinition tool)
    {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", tool.getName(),
                "description", tool.getDescription(),
                "parameters", ToolSchemaJson.toJsonSchema(tool)));
    }

    private static void ensureSuccess(int status, String body)
    {
        if (status >= 200 && status < 300)
        {
            return;
        }
        throw new IllegalStateException("Provider call failed with status=" + status + ", body=" + body);
    }

    private static class ToolDelta
    {
        private String name;
        private final StringBuilder arguments = new StringBuilder();
    }
}
