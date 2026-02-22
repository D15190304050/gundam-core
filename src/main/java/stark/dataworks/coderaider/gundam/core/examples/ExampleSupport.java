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
        return AgentRunner.builder(client, toolRegistry, agentRegistry)
            .contextBuilder(new DefaultContextBuilder())
            .hookManager(new HookManager())
            .guardrailEngine(new GuardrailEngine())
            .handoffRouter(new HandoffRouter())
            .sessionStore(sessionStore == null ? new InMemorySessionStore() : sessionStore)
            .traceProvider(new NoopTraceProvider())
            .toolApprovalPolicy(new AllowAllToolApprovalPolicy())
            .outputSchemaRegistry(new OutputSchemaRegistry())
            .outputValidator(new OutputValidator())
            .eventPublisher(new RunEventPublisher())
            .build();
    }

    static AgentRunner runnerWithPublisher(ILlmClient client, IToolRegistry toolRegistry, IAgentRegistry agentRegistry, ISessionStore sessionStore, RunEventPublisher publisher)
    {
        return AgentRunner.builder(client, toolRegistry, agentRegistry)
            .contextBuilder(new DefaultContextBuilder())
            .hookManager(new HookManager())
            .guardrailEngine(new GuardrailEngine())
            .handoffRouter(new HandoffRouter())
            .sessionStore(sessionStore == null ? new InMemorySessionStore() : sessionStore)
            .traceProvider(new NoopTraceProvider())
            .toolApprovalPolicy(new AllowAllToolApprovalPolicy())
            .outputSchemaRegistry(new OutputSchemaRegistry())
            .outputValidator(new OutputValidator())
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
