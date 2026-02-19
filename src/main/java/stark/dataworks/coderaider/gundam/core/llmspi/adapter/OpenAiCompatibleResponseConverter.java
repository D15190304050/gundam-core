package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class OpenAiCompatibleResponseConverter
{
    private OpenAiCompatibleResponseConverter()
    {
    }

    static LlmResponse fromChatResponse(ObjectMapper mapper, String rawJson) throws IOException
    {
        JsonNode root = mapper.readTree(rawJson);
        JsonNode choice0 = root.path("choices").isArray() && root.path("choices").size() > 0 ? root.path("choices").get(0) : mapper.createObjectNode();
        JsonNode message = choice0.path("message");
        String content = text(message.path("content"));
        String finishReason = text(choice0.path("finish_reason"));

        List<ToolCall> toolCalls = parseToolCalls(mapper, message.path("tool_calls"));
        String handoff = parseHandoff(content, toolCalls);

        JsonNode usage = root.path("usage");
        TokenUsage tokenUsage = new TokenUsage(usage.path("prompt_tokens").asInt(0), usage.path("completion_tokens").asInt(0));

        return new LlmResponse(content, toolCalls, handoff, tokenUsage, finishReason, Map.of());
    }

    static String parseHandoff(String content, List<ToolCall> toolCalls)
    {
        if (content != null && !content.isBlank())
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(content);
                String a = text(node.path("handoff_agent_id"));
                if (!a.isBlank())
                {
                    return a;
                }
                String b = text(node.path("handoffAgentId"));
                if (!b.isBlank())
                {
                    return b;
                }
            }
            catch (Exception ignored)
            {
            }
        }

        for (ToolCall call : toolCalls)
        {
            if ("handoff".equalsIgnoreCase(call.getToolName()))
            {
                Object to = call.getArguments().get("to_agent");
                if (to == null)
                {
                    to = call.getArguments().get("agentId");
                }
                if (to != null)
                {
                    return String.valueOf(to);
                }
            }
        }
        return null;
    }

    private static List<ToolCall> parseToolCalls(ObjectMapper mapper, JsonNode toolCallsNode)
    {
        List<ToolCall> toolCalls = new ArrayList<>();
        if (!toolCallsNode.isArray())
        {
            return toolCalls;
        }

        for (JsonNode toolCallNode : toolCallsNode)
        {
            JsonNode fn = toolCallNode.path("function");
            String name = text(fn.path("name"));
            String argsJson = text(fn.path("arguments"));
            if (name.isBlank())
            {
                continue;
            }

            Map<String, Object> args = new HashMap<>();
            if (!argsJson.isBlank())
            {
                try
                {
                    JsonNode argsNode = mapper.readTree(argsJson);
                    if (argsNode.isObject())
                    {
                        args = mapper.convertValue(argsNode, Map.class);
                    }
                }
                catch (Exception ignored)
                {
                    args.put("raw", argsJson);
                }
            }

            toolCalls.add(new ToolCall(name, args));
        }

        return toolCalls;
    }

    static String text(JsonNode node)
    {
        if (node == null || node.isNull())
        {
            return "";
        }
        if (node.isTextual())
        {
            return node.asText();
        }
        return node.toString();
    }
}
