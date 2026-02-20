package stark.dataworks.coderaider.gundam.core.examples;

import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.AllowAllToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.session.ISessionStore;
import stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;
import stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider;

final class ExampleSupport
{
    private ExampleSupport()
    {
    }

    static AgentRunner runner(ILlmClient client, IToolRegistry toolRegistry, IAgentRegistry agentRegistry, ISessionStore sessionStore)
    {
        return new AgentRunner(
            client,
            toolRegistry,
            agentRegistry,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            sessionStore == null ? new InMemorySessionStore() : sessionStore,
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            new RunEventPublisher());
    }

    static AgentRunner runnerWithPublisher(ILlmClient client, IToolRegistry toolRegistry, IAgentRegistry agentRegistry, ISessionStore sessionStore, RunEventPublisher publisher)
    {
        return new AgentRunner(
            client,
            toolRegistry,
            agentRegistry,
            new DefaultContextBuilder(),
            new HookManager(),
            new GuardrailEngine(),
            new HandoffRouter(),
            sessionStore == null ? new InMemorySessionStore() : sessionStore,
            new NoopTraceProvider(),
            new AllowAllToolApprovalPolicy(),
            new OutputSchemaRegistry(),
            new OutputValidator(),
            publisher);
    }

    static IRunHooks noopHooks()
    {
        return new IRunHooks()
        {
        };
    }
}
