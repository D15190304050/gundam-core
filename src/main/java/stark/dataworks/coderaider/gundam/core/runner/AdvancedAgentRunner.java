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
import stark.dataworks.coderaider.gundam.core.llmspi.LlmStreamListener;
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

/**
 * Orchestrates the full multi-turn agent run lifecycle.
 * <p>
 * This runner combines context rendering, model calls, tool execution, guardrails, handoff routing, retry policy,
 * tracing, and event publication so callers can execute a complete OpenAI-Agents-style loop through one entry point.
 */
public class AdvancedAgentRunner
{

    /**
     * Internal state for llm client; used while coordinating runtime behavior.
     */
    private final ILlmClient llmClient;

    /**
     * Internal state for tool registry; used while coordinating runtime behavior.
     */
    private final IToolRegistry toolRegistry;

    /**
     * Internal state for agent registry; used while coordinating runtime behavior.
     */
    private final IAgentRegistry agentRegistry;

    /**
     * Internal state for context builder; used while coordinating runtime behavior.
     */
    private final IContextBuilder contextBuilder;

    /**
     * Internal state for hook manager; used while coordinating runtime behavior.
     */
    private final HookManager hookManager;

    /**
     * Internal state for guardrail engine; used while coordinating runtime behavior.
     */
    private final GuardrailEngine guardrailEngine;

    /**
     * Internal state for handoff router; used while coordinating runtime behavior.
     */
    private final HandoffRouter handoffRouter;

    /**
     * Internal state for session store; used while coordinating runtime behavior.
     */
    private final SessionStore sessionStore;

    /**
     * Internal state for trace provider; used while coordinating runtime behavior.
     */
    private final TraceProvider traceProvider;

    /**
     * Internal state for tool approval policy; used while coordinating runtime behavior.
     */
    private final ToolApprovalPolicy toolApprovalPolicy;

    /**
     * Internal state for output schema registry; used while coordinating runtime behavior.
     */
    private final OutputSchemaRegistry outputSchemaRegistry;

    /**
     * Internal state for output validator; used while coordinating runtime behavior.
     */
    private final OutputValidator outputValidator;

    /**
     * Internal state for event publisher; used while coordinating runtime behavior.
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

    /**
     * Executes an agent session until a final answer is produced, a guardrail blocks execution, or limits are reached.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfig Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    public RunResult run(IAgent startingAgent, String userInput, RunConfig runConfig, RunHooks runHooks)
    {
        return runInternal(startingAgent, userInput, runConfig, runHooks, false);
    }

    /**
     * Executes an agent session with streamed model deltas while preserving the same final result contract.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfig Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    public RunResult runStreamed(IAgent startingAgent, String userInput, RunConfig runConfig, RunHooks runHooks)
    {
        return runInternal(startingAgent, userInput, runConfig, runHooks, true);
    }

    /**
     * Executes an agent run either in synchronous mode or in streaming mode based on the supplied flag.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfig Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @param streamModelResponse Whether model output should be streamed as delta events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    private RunResult runInternal(IAgent startingAgent, String userInput, RunConfig runConfig, RunHooks runHooks, boolean streamModelResponse)
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
                LlmResponse response = invokeModel(request, runConfig, streamModelResponse, delta ->
                    emit(context, runHooks, RunEventType.MODEL_RESPONSE_DELTA, Map.of("delta", delta)));
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

    /**
     * Performs handle error as part of AdvancedAgentRunner runtime responsibilities.
     * @param context The context used by this operation.
     * @param config The config used by this operation.
     * @param error The error used by this operation.
     * @return The value produced by this operation.
     */
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

    /**
     * Maps a runtime exception to a stable error category for handler selection.
     * @param error The error used by this operation.
     * @return The value produced by this operation.
     */
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

    /**
     * Invokes with retry while applying configured retry/backoff behavior.
     * @param request The request used by this operation.
     * @param config The config used by this operation.
     * @param streamModelResponse The stream model response used by this operation.
     * @param streamListener The stream listener used by this operation.
     * @return The value produced by this operation.
     */
    private LlmResponse invokeModel(LlmRequest request,
                                    RunConfig config,
                                    boolean streamModelResponse,
                                    LlmStreamListener streamListener)
    {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= config.getRetryPolicy().getMaxAttempts(); attempt++)
        {
            TraceSpan span = traceProvider.startSpan("agent.model_call");
            try
            {
                LlmResponse response = streamModelResponse
                    ? llmClient.chatStream(request, streamListener)
                    : llmClient.chat(request);
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

    /**
     * Resolves tools from configured registries before execution continues.
     * @param toolNames The tool names used by this operation.
     * @return The value produced by this operation.
     */
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

    /**
     * Publishes a runtime event so hooks/listeners can observe progress.
     * @param context The context used by this operation.
     * @param hooks The hooks used by this operation.
     * @param type The type used by this operation.
     * @param attributes The attributes used by this operation.
     */
    private void emit(RunnerContext context, RunHooks hooks, RunEventType type, Map<String, Object> attributes)
    {
        RunEvent event = new RunEvent(type, attributes);
        context.getEvents().add(event);
        hooks.onEvent(event);
        eventPublisher.publish(event);
    }

    /**
     * Performs finalize result as part of AdvancedAgentRunner runtime responsibilities.
     * @param context The context used by this operation.
     * @param finalOutput The final output used by this operation.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
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
