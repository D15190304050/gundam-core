package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.DistributedTraceEvent;
import stark.dataworks.coderaider.gundam.core.tracing.DistributedTraceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 14) Multi-round distributed tracing (same session) with a real LLM call.
 */
public class Example14TracingMultiRoundTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        Assertions.assertNotNull(apiKey, "MODEL_SCOPE_API_KEY is required");

        AgentDefinition definition = new AgentDefinition();
        definition.setId("trace-multi");
        definition.setName("trace-multi");
        definition.setModel(model);
        definition.setSystemPrompt("You are concise.");

        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.register(new Agent(definition));

        List<DistributedTraceEvent> spans = new ArrayList<>();
        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .traceProvider(new DistributedTraceProvider(spans::add))
            .build();

        RunConfiguration cfg = new RunConfiguration(12, "trace-session", 0.2, 512, "auto", "text", Map.of());
        String first = runner.chatClient("trace-multi")
            .prompt()
            .user("Round 1: Explain what a trace ID is in one sentence.")
            .runConfiguration(cfg)
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();
        String second = runner.chatClient("trace-multi")
            .prompt()
            .user("Round 2: Explain what a span ID is in one sentence.")
            .runConfiguration(cfg)
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();

        Assertions.assertFalse(first.isBlank());
        Assertions.assertFalse(second.isBlank());

        long runSpans = spans.stream().filter(s -> "agent.run".equals(s.spanName())).count();
        long modelSpans = spans.stream().filter(s -> "agent.model_call".equals(s.spanName())).count();
        Assertions.assertTrue(runSpans >= 2);
        Assertions.assertTrue(modelSpans >= 2);

        spans.forEach(span -> System.out.printf("trace=%s span=%s parent=%s name=%s attrs=%s%n",
            span.traceId(), span.spanId(), span.parentSpanId(), span.spanName(), span.attributes()));
    }
}
