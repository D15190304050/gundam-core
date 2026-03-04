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

/**
 * 13) Single-turn distributed tracing with a real LLM call.
 */
public class Example13TracingSingleTurnTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        Assertions.assertNotNull(apiKey, "MODEL_SCOPE_API_KEY is required");

        AgentDefinition definition = new AgentDefinition();
        definition.setId("trace-single");
        definition.setName("trace-single");
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

        String output = runner.chatClient("trace-single")
            .prompt()
            .user("Explain distributed tracing in one sentence.")
            .runConfiguration(RunConfiguration.defaults())
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();

        Assertions.assertFalse(output.isBlank());
        Assertions.assertTrue(spans.stream().anyMatch(s -> "agent.run".equals(s.spanName())));
        Assertions.assertTrue(spans.stream().anyMatch(s -> "agent.model_call".equals(s.spanName())));

        spans.forEach(span -> System.out.printf("trace=%s span=%s parent=%s name=%s attrs=%s%n",
            span.traceId(), span.spanId(), span.parentSpanId(), span.spanName(), span.attributes()));
    }
}
