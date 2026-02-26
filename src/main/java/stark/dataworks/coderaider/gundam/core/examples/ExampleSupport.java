package stark.dataworks.coderaider.gundam.core.examples;

import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.session.ISessionStore;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;

final class ExampleSupport
{
    private ExampleSupport()
    {
    }

    static AgentRunner runner(ILlmClient client, IToolRegistry toolRegistry, IAgentRegistry agentRegistry, ISessionStore sessionStore)
    {
        return AgentRunner.builder()
            .llmClient(client)
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .sessionStore(sessionStore == null ? new InMemorySessionStore() : sessionStore)
            .build();
    }

    static AgentRunner runnerWithPublisher(ILlmClient client, IToolRegistry toolRegistry, IAgentRegistry agentRegistry, ISessionStore sessionStore, RunEventPublisher publisher)
    {
        return AgentRunner.builder()
            .llmClient(client)
            .toolRegistry(toolRegistry)
            .agentRegistry(agentRegistry)
            .sessionStore(sessionStore == null ? new InMemorySessionStore() : sessionStore)
            .eventPublisher(publisher)
            .build();
    }


    static IRunHooks noopHooks()
    {
        return new IRunHooks()
        {
        };
    }
}
