package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;

import java.util.List;
import java.util.Map;

class OpenAiCompatibleResponseConverterTest
{
    @Test
    void shouldConvertToolCallsFromNativeResponse() throws Exception
    {
        String json = """
            {
              "choices": [
                {
                  "finish_reason": "tool_calls",
                  "message": {
                    "content": "",
                    "tool_calls": [
                      {
                        "id": "call_1",
                        "type": "function",
                        "function": {
                          "name": "weather_lookup",
                          "arguments": "{\\\"city\\\":\\\"Shanghai\\\"}"
                        }
                      }
                    ]
                  }
                }
              ],
              "usage": {"prompt_tokens": 12, "completion_tokens": 8}
            }
            """;

        LlmResponse response = OpenAiCompatibleResponseConverter.fromChatResponse(new ObjectMapper(), json);
        Assertions.assertEquals(1, response.getToolCalls().size());
        Assertions.assertEquals("weather_lookup", response.getToolCalls().get(0).getToolName());
        Assertions.assertEquals("Shanghai", response.getToolCalls().get(0).getArguments().get("city"));
        Assertions.assertTrue(response.getHandoffAgentId().isEmpty());
    }


    @Test
    void shouldExtractReasoningContentFromResponse()
            throws Exception
    {
        String json = """
            {
              "choices": [
                {
                  "finish_reason": "stop",
                  "message": {
                    "content": "final",
                    "reasoning_content": "step-by-step"
                  }
                }
              ],
              "usage": {"prompt_tokens": 3, "completion_tokens": 4}
            }
            """;

        LlmResponse response = OpenAiCompatibleResponseConverter.fromChatResponse(new ObjectMapper(), json);
        Assertions.assertEquals("step-by-step", response.getReasoningContent());
    }

    @Test
    void shouldExtractHandoffFromStructuredContent()
    {
        String handoff = OpenAiCompatibleResponseConverter.parseHandoff("{\"handoff_agent_id\":\"billing-agent\"}", List.of());
        Assertions.assertEquals("billing-agent", handoff);
    }

    @Test
    void shouldExtractHandoffFromHandoffToolCall()
    {
        String handoff = OpenAiCompatibleResponseConverter.parseHandoff("", List.of(
            new stark.dataworks.coderaider.gundam.core.model.ToolCall("handoff", Map.of("to_agent", "planner"))));
        Assertions.assertEquals("planner", handoff);
    }
}
