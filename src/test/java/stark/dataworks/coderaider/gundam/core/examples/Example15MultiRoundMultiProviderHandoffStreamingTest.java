package stark.dataworks.coderaider.gundam.core.examples;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmClientRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.SeedLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 15) Single-session, multi-round handoff across different providers/models with streaming output.
 */
public class Example15MultiRoundMultiProviderHandoffStreamingTest
{
    @Test
    public void run()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String volcKey = env.get("VOLCENGINE_API_KEY", System.getenv("VOLCENGINE_API_KEY"));
        String modelScopeKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));

        if (volcKey == null || volcKey.isBlank() || modelScopeKey == null || modelScopeKey.isBlank())
        {
            System.out.println("Skipping integration example: missing VOLCENGINE_API_KEY or MODEL_SCOPE_API_KEY");
            return;
        }

        AgentDefinition doubaoAgent = new AgentDefinition();
        doubaoAgent.setId("doubao-triage");
        doubaoAgent.setName("Doubao Triage Agent");
        doubaoAgent.setModel("doubao-1-5-pro-32k-250115");
        doubaoAgent.setSystemPrompt("You are a triage agent. Always handoff to qwen-specialist by returning exactly: handoff: qwen-specialist");
        doubaoAgent.setHandoffAgentIds(List.of("qwen-specialist"));

        AgentDefinition qwenAgent = new AgentDefinition();
        qwenAgent.setId("qwen-specialist");
        qwenAgent.setName("Qwen Specialist Agent");
        qwenAgent.setModel("Qwen/Qwen3-4B");
        qwenAgent.setSystemPrompt("You are a specialist agent. Produce concise useful answers.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(doubaoAgent));
        registry.register(new Agent(qwenAgent));

        AgentRunner runner = AgentRunner.builder()
            .llmClientRegistry(new LlmClientRegistry(
                Map.of(
                    "doubao-1-5-pro-32k-250115", new SeedLlmClient(volcKey, "doubao-1-5-pro-32k-250115"),
                    "Qwen", new ModelScopeLlmClient(modelScopeKey, "Qwen/Qwen3-4B")),
                "Qwen"))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .sessionStore(new InMemorySessionStore())
            .eventPublisher(ExampleStreamingPublishers.textOnly())
            .build();

        RunConfiguration sessionCfg = new RunConfiguration(8, "mix-provider-session", 0.2, 512, "auto", "text", Map.of());
        ContextResult round1 = runner.runStreamed(new Agent(doubaoAgent), "Please draft a migration checklist.", sessionCfg, ExampleSupport.noopHooks());
        ContextResult round2 = runner.runStreamed(new Agent(doubaoAgent), "Now summarize it in 3 bullets.", sessionCfg, ExampleSupport.noopHooks());

        assertFalse(round1.getFinalOutput().isBlank());
        assertFalse(round2.getFinalOutput().isBlank());
    }
}
