package stark.dataworks.coderaider.gundam.core.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.ToolApprovalDecision;
import stark.dataworks.coderaider.gundam.core.approval.ToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.approval.ToolApprovalRequest;
import stark.dataworks.coderaider.gundam.core.context.IContextBuilder;
import stark.dataworks.coderaider.gundam.core.errors.GuardrailTripwireException;
import stark.dataworks.coderaider.gundam.core.errors.HandoffDeniedException;
import stark.dataworks.coderaider.gundam.core.errors.MaxTurnsExceededException;
import stark.dataworks.coderaider.gundam.core.errors.ModelInvocationException;
import stark.dataworks.coderaider.gundam.core.errors.ToolExecutionFailureException;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailDecision;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.Handoff;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidationResult;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.result.RunItem;
import stark.dataworks.coderaider.gundam.core.result.RunItemType;
import stark.dataworks.coderaider.gundam.core.result.RunResult;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorData;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlerResult;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorKind;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
import stark.dataworks.coderaider.gundam.core.session.Session;
import stark.dataworks.coderaider.gundam.core.session.SessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tracing.TraceProvider;
import stark.dataworks.coderaider.gundam.core.tracing.TraceSpan;

public class AdvancedAgentRunner
{
    /**
     * Field llmClient.
     */
    private final ILlmClient llmClient;
    /**
     * Field toolRegistry.
     */
    private final IToolRegistry toolRegistry;
    /**
     * Field agentRegistry.
     */
    private final IAgentRegistry agentRegistry;
    /**
     * Field contextBuilder.
     */
    private final IContextBuilder contextBuilder;
    /**
     * Field hookManager.
     */
    private final HookManager hookManager;
    /**
     * Field guardrailEngine.
     */
    private final GuardrailEngine guardrailEngine;
    /**
     * Field handoffRouter.
     */
    private final HandoffRouter handoffRouter;
    /**
     * Field sessionStore.
     */
    private final SessionStore sessionStore;
    /**
     * Field traceProvider.
     */
    private final TraceProvider traceProvider;
    /**
     * Field toolApprovalPolicy.
     */
    private final ToolApprovalPolicy toolApprovalPolicy;
    /**
     * Field outputSchemaRegistry.
     */
    private final OutputSchemaRegistry outputSchemaRegistry;
    /**
     * Field outputValidator.
     */
    private final OutputValidator outputValidator;
    /**
     * Field eventPublisher.
     */
    private final RunEventPublisher eventPublisher;

    public AdvancedAgentRunner(ILlmClient llmClient,
                               IToolRegistry toolRegistry,
                               IAgentRegistry agentRegistry,
                               IContextBuilder contextBuilder,
                               HookManager hookManager,
                               GuardrailEngine guardrailEngine,
                               HandoffRouter handoffRouter,
                               SessionStore sessionStore,
                               TraceProvider traceProvider,
                               ToolApprovalPolicy toolApprovalPolicy,
                               OutputSchemaRegistry outputSchemaRegistry,
                               OutputValidator outputValidator,
                               RunEventPublisher eventPublisher)
    {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentRegistry = agentRegistry;
        this.contextBuilder = contextBuilder;
        this.hookManager = hookManager;
        this.guardrailEngine = guardrailEngine;
        this.handoffRouter = handoffRouter;
        this.sessionStore = sessionStore;
        this.traceProvider = traceProvider;
        this.toolApprovalPolicy = toolApprovalPolicy;
        this.outputSchemaRegistry = outputSchemaRegistry;
        this.outputValidator = outputValidator;
        this.eventPublisher = eventPublisher;
    }

    public RunResult run(IAgent startingAgent, String userInput, RunConfig runConfig, RunHooks runHooks)
    {
        IAgentMemory memory = new InMemoryAgentMemory();
        if (runConfig.getSessionId() != null)
        {
            sessionStore.load(runConfig.getSessionId()).ifPresent(s -> s.getMessages().forEach(memory::append));
        }

        RunnerContext context = new RunnerContext(startingAgent, memory);
        emit(context, runHooks, RunEventType.RUN_STARTED, Map.of("agent", startingAgent.definition().getId()));

        if (userInput != null && !userInput.isBlank())
        {
            context.getMemory().append(new Message(Role.USER, userInput));
            context.getItems().add(new RunItem(RunItemType.USER_MESSAGE, userInput, Map.of()));
        }

        try
        {
            while (context.getTurns() < runConfig.getMaxTurns())
            {
                context.incrementTurns();
                emit(context, runHooks, RunEventType.STEP_STARTED, Map.of("turn", context.getTurns()));

                ExecutionContext legacyContext = new ExecutionContext(context.getCurrentAgent(), context.getMemory(), context.getUsageTracker());
                GuardrailDecision inputDecision = guardrailEngine.evaluateInput(legacyContext, userInput);
                if (!inputDecision.isAllowed())
                {
                    throw new GuardrailTripwireException("input", inputDecision.getReason());
                }

                List<Message> messages = contextBuilder.build(context.getCurrentAgent(), context.getMemory(), null);
                List<ToolDefinition> toolDefinitions = resolveTools(context.getCurrentAgent().definition().getToolNames());
                LlmRequest request = new LlmRequest(context.getCurrentAgent().definition().getModel(), messages, toolDefinitions,
                    new LlmOptions(runConfig.getTemperature(), runConfig.getMaxOutputTokens(), runConfig.getToolChoice(), runConfig.getResponseFormat(), runConfig.getProviderOptions()));

                emit(context, runHooks, RunEventType.MODEL_REQUESTED, Map.of("model", request.getModel(), "messages", request.getMessages().size()));
                LlmResponse response = invokeWithRetry(request, runConfig);
                emit(context, runHooks, RunEventType.MODEL_RESPONDED, Map.of("finishReason", response.getFinishReason()));
                context.getUsageTracker().add(response.getTokenUsage());

                GuardrailDecision outputDecision = guardrailEngine.evaluateOutput(legacyContext, response);
                if (!outputDecision.isAllowed())
                {
                    throw new GuardrailTripwireException("output", outputDecision.getReason());
                }

                if (!response.getContent().isBlank())
                {
                    context.getMemory().append(new Message(Role.ASSISTANT, response.getContent()));
                    context.getItems().add(new RunItem(RunItemType.ASSISTANT_MESSAGE, response.getContent(), response.getStructuredOutput()));
                }

                if (response.getHandoffAgentId().isPresent())
                {
                    String toAgent = response.getHandoffAgentId().get();
                    Handoff handoff = new Handoff(context.getCurrentAgent().definition().getId(), toAgent, "model requested handoff");
                    if (!context.getCurrentAgent().definition().getHandoffAgentIds().contains(toAgent) || !handoffRouter.canRoute(handoff))
                    {
                        throw new HandoffDeniedException(handoff.getFromAgentId(), toAgent);
                    }

                    IAgent next = agentRegistry.get(toAgent)
                        .orElseThrow(() -> new IllegalStateException("Handoff agent not found: " + toAgent));
                    context.setCurrentAgent(next);
                    context.getItems().add(new RunItem(RunItemType.HANDOFF, toAgent, Map.of("from", handoff.getFromAgentId())));
                    emit(context, runHooks, RunEventType.HANDOFF_OCCURRED, Map.of("from", handoff.getFromAgentId(), "to", toAgent));
                    if (context.getCurrentAgent().definition().isResetInputAfterHandoff())
                    {
                        userInput = null;
                    }
                    continue;
                }

                if (!response.getToolCalls().isEmpty())
                {
                    for (ToolCall call : response.getToolCalls())
                    {
                        emit(context, runHooks, RunEventType.TOOL_CALL_REQUESTED, Map.of("tool", call.getToolName()));

                        if (context.getCurrentAgent().definition().isRequireToolApproval())
                        {
                            ToolApprovalDecision decision = toolApprovalPolicy.decide(
                                new ToolApprovalRequest(context.getCurrentAgent().definition().getId(), call.getToolName(), call.getArguments()));
                            if (!decision.isApproved())
                            {
                                context.getItems().add(new RunItem(RunItemType.SYSTEM_EVENT,
                                    "Tool call denied: " + decision.getReason(), Map.of("tool", call.getToolName())));
                                continue;
                            }
                        }

                        ITool tool = toolRegistry.get(call.getToolName())
                            .orElseThrow(() -> new IllegalStateException("Tool not found: " + call.getToolName()));
                        try
                        {
                            hookManager.beforeTool(call.getToolName(), call.getArguments());
                            String result = tool.execute(call.getArguments());
                            hookManager.afterTool(call.getToolName(), result);
                            context.getMemory().append(new Message(Role.TOOL, call.getToolName() + ": " + result));
                            context.getItems().add(new RunItem(RunItemType.TOOL_CALL, call.getToolName(), call.getArguments()));
                            context.getItems().add(new RunItem(RunItemType.TOOL_RESULT, result, Map.of("tool", call.getToolName())));
                            emit(context, runHooks, RunEventType.TOOL_CALL_COMPLETED, Map.of("tool", call.getToolName()));
                        }
                        catch (RuntimeException ex)
                        {
                            throw new ToolExecutionFailureException(call.getToolName(), ex);
                        }
                    }
                    if (context.getCurrentAgent().definition().isResetInputAfterToolCall())
                    {
                        userInput = null;
                    }
                    continue;
                }

                if (context.getCurrentAgent().definition().getOutputSchemaName() != null)
                {
                    OutputValidationResult validation = outputSchemaRegistry
                        .get(context.getCurrentAgent().definition().getOutputSchemaName())
                        .map(schema -> outputValidator.validate(response.getStructuredOutput(), schema))
                        .orElse(OutputValidationResult.fail("Output schema not found: " + context.getCurrentAgent().definition().getOutputSchemaName()));
                    if (!validation.isValid())
                    {
                        context.getItems().add(new RunItem(RunItemType.SYSTEM_EVENT, "Structured output invalid: " + validation.getReason(), Map.of()));
                        continue;
                    }
                }

                return finalizeResult(context, response.getContent(), runConfig);
            }

            throw new MaxTurnsExceededException(runConfig.getMaxTurns());
        }
        catch (RuntimeException error)
        {
            return handleError(context, runConfig, error);
        }
    }

    private RunResult handleError(RunnerContext context, RunConfig config, RuntimeException error)
    {
        RunErrorKind kind = classify(error);
        RunErrorHandlerResult decision = config.getRunErrorHandlers().get(kind)
            .map(h -> h.onError(new RunErrorData(kind, error.getMessage(), error)))
            .orElse(RunErrorHandlerResult.notHandled());

        String finalOutput;
        if (decision.isHandled())
        {
            finalOutput = decision.getFinalOutput();
        }
        else if (kind == RunErrorKind.INPUT_GUARDRAIL)
        {
            finalOutput = "Blocked by input guardrail";
        }
        else if (kind == RunErrorKind.OUTPUT_GUARDRAIL)
        {
            finalOutput = "Blocked by output guardrail";
        }
        else
        {
            finalOutput = "Run failed: " + error.getMessage();
        }

        context.getItems().add(new RunItem(RunItemType.SYSTEM_EVENT, finalOutput, Map.of("errorKind", kind.name())));

        if (kind == RunErrorKind.INPUT_GUARDRAIL || kind == RunErrorKind.OUTPUT_GUARDRAIL)
        {
            emit(context, new RunHooks()
            {
            }, RunEventType.GUARDRAIL_BLOCKED, Map.of("kind", kind.name(), "message", error.getMessage()));
        }

        emit(context, new RunHooks()
        {
        }, RunEventType.RUN_FAILED, Map.of("kind", kind.name(), "message", error.getMessage()));
        return finalizeResult(context, finalOutput, config);
    }

    private RunErrorKind classify(RuntimeException error)
    {
        if (error instanceof MaxTurnsExceededException) return RunErrorKind.MAX_TURNS;
        if (error instanceof GuardrailTripwireException e)
        {
            return e.getMessage().contains("input") ? RunErrorKind.INPUT_GUARDRAIL : RunErrorKind.OUTPUT_GUARDRAIL;
        }
        if (error instanceof HandoffDeniedException) return RunErrorKind.HANDOFF;
        if (error instanceof ToolExecutionFailureException) return RunErrorKind.TOOL_EXECUTION;
        if (error instanceof ModelInvocationException) return RunErrorKind.MODEL_INVOCATION;
        return RunErrorKind.UNKNOWN;
    }

    private LlmResponse invokeWithRetry(LlmRequest request, RunConfig config)
    {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= config.getRetryPolicy().getMaxAttempts(); attempt++)
        {
            TraceSpan span = traceProvider.startSpan("agent.model_call");
            try
            {
                LlmResponse response = llmClient.chat(request);
                span.annotate("finish_reason", response.getFinishReason());
                span.annotate("attempt", String.valueOf(attempt));
                return response;
            }
            catch (RuntimeException ex)
            {
                last = ex;
                span.annotate("error", ex.getMessage());
                if (attempt < config.getRetryPolicy().getMaxAttempts() && config.getRetryPolicy().getBackoffMillis() > 0)
                {
                    try
                    {
                        Thread.sleep(config.getRetryPolicy().getBackoffMillis());
                    }
                    catch (InterruptedException interruptedException)
                    {
                        Thread.currentThread().interrupt();
                        throw new ModelInvocationException("Interrupted during model retry", interruptedException);
                    }
                }
            }
            finally
            {
                span.close();
            }
        }
        throw new ModelInvocationException("Model invocation failed after retries", last);
    }

    private List<ToolDefinition> resolveTools(List<String> toolNames)
    {
        List<ToolDefinition> definitions = new ArrayList<>();
        for (String toolName : toolNames)
        {
            definitions.add(toolRegistry.get(toolName)
                .orElseThrow(() -> new IllegalStateException("Tool not found: " + toolName))
                .definition());
        }
        return definitions;
    }

    private void emit(RunnerContext context, RunHooks hooks, RunEventType type, Map<String, Object> attributes)
    {
        RunEvent event = new RunEvent(type, attributes);
        context.getEvents().add(event);
        hooks.onEvent(event);
        eventPublisher.publish(event);
    }

    private RunResult finalizeResult(RunnerContext context, String finalOutput, RunConfig config)
    {
        if (config.getSessionId() != null)
        {
            sessionStore.save(new Session(config.getSessionId(), context.getMemory().messages()));
        }
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("finalAgent", context.getCurrentAgent().definition().getId());
        emit(context, new RunHooks()
        {
        }, RunEventType.RUN_COMPLETED, attrs);
        return new RunResult(
            finalOutput,
            context.getCurrentAgent().definition().getId(),
            context.getUsageTracker().snapshot(),
            context.getItems(),
            context.getEvents());
    }
}
