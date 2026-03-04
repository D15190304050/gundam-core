package stark.dataworks.coderaider.gundam.core.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;
import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.agent.IAgentRegistry;
import stark.dataworks.coderaider.gundam.core.approval.ToolApprovalDecision;
import stark.dataworks.coderaider.gundam.core.approval.IToolApprovalPolicy;
import stark.dataworks.coderaider.gundam.core.approval.ToolApprovalRequest;
import stark.dataworks.coderaider.gundam.core.agent.AgentChatClient;
import stark.dataworks.coderaider.gundam.core.context.IContextBuilder;
import stark.dataworks.coderaider.gundam.core.exceptions.GuardrailTripwireException;
import stark.dataworks.coderaider.gundam.core.exceptions.HandoffDeniedException;
import stark.dataworks.coderaider.gundam.core.exceptions.MaxTurnsExceededException;
import stark.dataworks.coderaider.gundam.core.exceptions.ModelInvocationException;
import stark.dataworks.coderaider.gundam.core.exceptions.ToolExecutionFailureException;
import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.event.RunEventType;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailDecision;
import stark.dataworks.coderaider.gundam.core.guardrail.GuardrailEngine;
import stark.dataworks.coderaider.gundam.core.handoff.Handoff;
import stark.dataworks.coderaider.gundam.core.handoff.HandoffRouter;
import stark.dataworks.coderaider.gundam.core.hook.HookManager;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmClientRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.memory.IAgentMemory;
import stark.dataworks.coderaider.gundam.core.memory.policy.MemoryLifecyclePolicy;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.memory.InMemoryAgentMemory;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.model.ToolCall;
import stark.dataworks.coderaider.gundam.core.output.IOutputSchema;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaMapper;
import stark.dataworks.coderaider.gundam.core.output.OutputSchemaRegistry;
import stark.dataworks.coderaider.gundam.core.output.OutputValidationResult;
import stark.dataworks.coderaider.gundam.core.output.OutputValidator;
import stark.dataworks.coderaider.gundam.core.context.ContextItem;
import stark.dataworks.coderaider.gundam.core.context.ContextItemType;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorData;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorHandlerResult;
import stark.dataworks.coderaider.gundam.core.runerror.RunErrorKind;
import stark.dataworks.coderaider.gundam.core.runtime.ExecutionContext;
import stark.dataworks.coderaider.gundam.core.session.Session;
import stark.dataworks.coderaider.gundam.core.session.ISessionStore;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.IToolRegistry;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tracing.ITraceProvider;
import stark.dataworks.coderaider.gundam.core.tracing.ITraceSpan;

/**
 * Orchestrates the full multi-turn agent run lifecycle.
 * <p>
 * This runner combines context rendering, model calls, tool execution, guardrails, handoff routing, retry policy,
 * tracing, and event publication so callers can execute a complete OpenAI-Agents-style loop through one entry point.
 * <p>
 * Example builder usage:
 * <pre>{@code
 * AgentRunner runner = AgentRunner.builder()
 *     .llmClientRegistry(new LlmClientRegistry(Map.of("modelscope", llmClient), "modelscope"))
 *     .toolRegistry(toolRegistry)
 *     .agentRegistry(agentRegistry)
 *     .build();
 * }</pre>
 */
@AllArgsConstructor
public class AgentRunner
{
    /**
     * Llm client registry.
     */
    private final LlmClientRegistry llmClientRegistry;

    /**
     * Registry used to resolve tool definitions and executors.
     */
    private final IToolRegistry toolRegistry;

    /**
     * Registry used to resolve agents by id.
     */
    private final IAgentRegistry agentRegistry;

    /**
     * Builder that assembles runtime context and message history.
     */
    private final IContextBuilder contextBuilder;

    /**
     * Hook manager for run lifecycle callbacks.
     */
    private final HookManager hookManager;

    /**
     * Engine that evaluates configured guardrails.
     */
    private final GuardrailEngine guardrailEngine;

    /**
     * Router that selects and validates handoff targets.
     */
    private final HandoffRouter handoffRouter;

    /**
     * Store used to persist and reload sessions.
     */
    private final ISessionStore sessionStore;

    /**
     * Trace provider used to create spans for observability.
     */
    private final ITraceProvider traceProvider;

    /**
     * Policy that approves or rejects tool calls.
     */
    private final IToolApprovalPolicy toolApprovalPolicy;

    /**
     * Output schema registry.
     */
    private final OutputSchemaRegistry outputSchemaRegistry;

    /**
     * Validator used for structured-output schema checks.
     */
    private final OutputValidator outputValidator;

    /**
     * Publisher that emits streaming run events.
     */
    private final RunEventPublisher eventPublisher;


    public AgentRunner(ILlmClient llmClient,
                       IToolRegistry toolRegistry,
                       IAgentRegistry agentRegistry,
                       IContextBuilder contextBuilder,
                       HookManager hookManager,
                       GuardrailEngine guardrailEngine,
                       HandoffRouter handoffRouter,
                       ISessionStore sessionStore,
                       ITraceProvider traceProvider,
                       IToolApprovalPolicy toolApprovalPolicy,
                       OutputSchemaRegistry outputSchemaRegistry,
                       OutputValidator outputValidator,
                       RunEventPublisher eventPublisher)
    {
        this(LlmClientRegistry.single(llmClient), toolRegistry, agentRegistry, contextBuilder, hookManager, guardrailEngine,
            handoffRouter, sessionStore, traceProvider, toolApprovalPolicy, outputSchemaRegistry, outputValidator, eventPublisher);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private LlmClientRegistry llmClientRegistry;
        private IToolRegistry toolRegistry;
        private IAgentRegistry agentRegistry;
        private IContextBuilder contextBuilder = new stark.dataworks.coderaider.gundam.core.context.DefaultContextBuilder();
        private HookManager hookManager = new HookManager();
        private GuardrailEngine guardrailEngine = new GuardrailEngine();
        private HandoffRouter handoffRouter = new HandoffRouter();
        private ISessionStore sessionStore = new stark.dataworks.coderaider.gundam.core.session.InMemorySessionStore();
        private ITraceProvider traceProvider = new stark.dataworks.coderaider.gundam.core.tracing.NoopTraceProvider();
        private IToolApprovalPolicy toolApprovalPolicy = new stark.dataworks.coderaider.gundam.core.approval.AllowAllToolApprovalPolicy();
        private OutputSchemaRegistry outputSchemaRegistry = new OutputSchemaRegistry();
        private OutputValidator outputValidator = new OutputValidator();
        private RunEventPublisher eventPublisher = new RunEventPublisher();

        private Builder()
        {
        }

        public Builder llmClient(ILlmClient value) { this.llmClientRegistry = LlmClientRegistry.single(Objects.requireNonNull(value, "llmClient")); return this; }
        public Builder llmClientRegistry(LlmClientRegistry value) { this.llmClientRegistry = Objects.requireNonNull(value, "llmClientRegistry"); return this; }
        public Builder toolRegistry(IToolRegistry value) { this.toolRegistry = Objects.requireNonNull(value, "toolRegistry"); return this; }
        public Builder agentRegistry(IAgentRegistry value) { this.agentRegistry = Objects.requireNonNull(value, "agentRegistry"); return this; }
        public Builder contextBuilder(IContextBuilder value) { this.contextBuilder = Objects.requireNonNull(value); return this; }
        public Builder hookManager(HookManager value) { this.hookManager = Objects.requireNonNull(value); return this; }
        public Builder guardrailEngine(GuardrailEngine value) { this.guardrailEngine = Objects.requireNonNull(value); return this; }
        public Builder handoffRouter(HandoffRouter value) { this.handoffRouter = Objects.requireNonNull(value); return this; }
        public Builder sessionStore(ISessionStore value) { this.sessionStore = Objects.requireNonNull(value); return this; }
        public Builder traceProvider(ITraceProvider value) { this.traceProvider = Objects.requireNonNull(value); return this; }
        public Builder toolApprovalPolicy(IToolApprovalPolicy value) { this.toolApprovalPolicy = Objects.requireNonNull(value); return this; }
        public Builder outputSchemaRegistry(OutputSchemaRegistry value) { this.outputSchemaRegistry = Objects.requireNonNull(value); return this; }
        public Builder outputValidator(OutputValidator value) { this.outputValidator = Objects.requireNonNull(value); return this; }
        public Builder eventPublisher(RunEventPublisher value) { this.eventPublisher = Objects.requireNonNull(value); return this; }

        public AgentRunner build()
        {
            Objects.requireNonNull(llmClientRegistry, "llmClient or llmClientRegistry must be set");
            Objects.requireNonNull(toolRegistry, "toolRegistry must be set");
            Objects.requireNonNull(agentRegistry, "agentRegistry must be set");
            return new AgentRunner(llmClientRegistry, toolRegistry, agentRegistry, contextBuilder, hookManager, guardrailEngine, handoffRouter,
                sessionStore, traceProvider, toolApprovalPolicy, outputSchemaRegistry, outputValidator, eventPublisher);
        }
    }


    public IAgentRegistry getAgentRegistry()
    {
        return agentRegistry;
    }

    public AgentChatClient chatClient(String defaultAgentId)
    {
        return AgentChatClient.create(this, defaultAgentId);
    }

    /**
     * Executes an agent session until a final answer is produced, a guardrail blocks execution, or limits are reached.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfiguration Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    public ContextResult run(IAgent startingAgent, String userInput, RunConfiguration runConfiguration, IRunHooks runHooks)
    {
        return run(startingAgent, userInput, runConfiguration, runHooks, null);
    }

    public ContextResult run(IAgent startingAgent, String userInput, RunConfiguration runConfiguration, IRunHooks runHooks, Class<?> outputType)
    {
        return runInternal(startingAgent, userInput, runConfiguration, runHooks, false, outputType);
    }

    /**
     * Executes an agent session with streamed model deltas while preserving the same final result contract.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfiguration Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    public ContextResult runStreamed(IAgent startingAgent, String userInput, RunConfiguration runConfiguration, IRunHooks runHooks)
    {
        return runStreamed(startingAgent, userInput, runConfiguration, runHooks, null);
    }

    public ContextResult runStreamed(IAgent startingAgent, String userInput, RunConfiguration runConfiguration, IRunHooks runHooks, Class<?> outputType)
    {
        return runInternal(startingAgent, userInput, runConfiguration, runHooks, true, outputType);
    }

    /**
     * Executes an agent run either in synchronous mode or in streaming mode based on the supplied flag.
     * @param startingAgent First agent that receives the user request.
     * @param userInput Initial user message.
     * @param runConfiguration Run limits, generation options, retry policy, and error-handler configuration.
     * @param runHooks Lifecycle callbacks invoked for run/step/tool events.
     * @param streamModelResponse Whether model output should be streamed as delta events.
     * @return Final run output, agent id, usage metrics, emitted events, and timeline items.
     */
    private ContextResult runInternal(IAgent startingAgent, String userInput, RunConfiguration runConfiguration, IRunHooks runHooks, boolean streamModelResponse, Class<?> outputType)
    {
        ITraceSpan runSpan = traceProvider.startSpan("agent.run");
        runSpan.annotate("agent", startingAgent.definition().getId());
        IAgentMemory memory = runConfiguration.getAgentMemory() == null
            ? new InMemoryAgentMemory()
            : runConfiguration.getAgentMemory();
        MemoryLifecyclePolicy memoryPolicy = runConfiguration.getMemoryLifecyclePolicy() == null
            ? MemoryLifecyclePolicy.noop()
            : runConfiguration.getMemoryLifecyclePolicy();
        if (runConfiguration.getSessionId() != null)
        {
            sessionStore.load(runConfiguration.getSessionId())
                .ifPresent(s -> s.getMessages().forEach(message -> appendToMemory(contextForBootstrap(startingAgent, memory), runHooks,
                    message, runConfiguration.getSessionId(), memoryPolicy, false)));
        }

        RunnerContext context = new RunnerContext(startingAgent, memory);
        ExecutionContext legacyContext = new ExecutionContext(context.getCurrentAgent(), context.getMemory(), context.getUsageTracker());
        hookManager.beforeRun(legacyContext);
        emit(context, runHooks, RunEventType.RUN_STARTED, Map.of("agent", startingAgent.definition().getId()));

        if (userInput != null && !userInput.isBlank())
        {
            appendToMemory(context, runHooks, new Message(Role.USER, userInput), runConfiguration.getSessionId(), memoryPolicy, true);
            context.getItems().add(new ContextItem(ContextItemType.USER_MESSAGE, userInput, Map.of()));
        }

        try
        {
            while (context.getTurns() < runConfiguration.getMaxTurns())
            {
                context.incrementTurns();
                emit(context, runHooks, RunEventType.STEP_STARTED, Map.of("turn", context.getTurns()));
                legacyContext.setAgent(context.getCurrentAgent());
                hookManager.onStep(legacyContext);
                GuardrailDecision inputDecision = guardrailEngine.evaluateInput(legacyContext, userInput);
                if (!inputDecision.isAllowed())
                {
                    throw new GuardrailTripwireException("input", inputDecision.getReason());
                }

                List<Message> messages = contextBuilder.build(context.getCurrentAgent(), new SnapshotAgentMemory(readFromMemory(context, runHooks, runConfiguration.getSessionId(), memoryPolicy)), null);
                List<ToolDefinition> toolDefinitions = resolveTools(context.getCurrentAgent().definition().getToolNames());
                Map<String, Object> providerOptions = new HashMap<>(context.getCurrentAgent().definition().getModelProviderOptions());
                providerOptions.putAll(runConfiguration.getProviderOptions());
                if (!context.getCurrentAgent().definition().getModelReasoning().isEmpty())
                {
                    providerOptions.putIfAbsent("reasoning", context.getCurrentAgent().definition().getModelReasoning());
                }
                if (!context.getCurrentAgent().definition().getModelSkills().isEmpty())
                {
                    providerOptions.putIfAbsent("skills", context.getCurrentAgent().definition().getModelSkills());
                }

                IOutputSchema classSchema = outputType == null ? null : OutputSchemaMapper.fromClass(outputType);
                String responseFormat = outputType == null ? runConfiguration.getResponseFormat() : "json_schema";
                if (outputType != null)
                {
                    providerOptions.put("responseFormatJsonSchema", OutputSchemaMapper.toOpenAiJsonSchema(outputType));
                }

                ILlmClient llmClient = llmClientRegistry.resolve(context.getCurrentAgent().definition().getModel(), providerOptions);
                Map<String, Object> providerOptionsForRequest = new HashMap<>(providerOptions);
                providerOptionsForRequest.remove("llmClient");

                LlmRequest request = new LlmRequest(context.getCurrentAgent().definition().getModel(), messages, toolDefinitions,
                    new LlmOptions(runConfiguration.getTemperature(), runConfiguration.getMaxOutputTokens(), runConfiguration.getToolChoice(), responseFormat, providerOptionsForRequest));

                emit(context, runHooks, RunEventType.MODEL_REQUESTED, Map.of("model", request.getModel(), "messages", request.getMessages().size()));
                StreamCapture streamCapture = new StreamCapture();
                LlmResponse response = invokeModel(llmClient, request, runConfiguration, streamModelResponse, streamListenerForRunner(context, runHooks, streamCapture));
                LlmResponse effectiveResponse = mergeWithStreamedResponse(response, streamCapture);
                emit(context, runHooks, RunEventType.MODEL_RESPONDED, Map.of("finishReason", effectiveResponse.getFinishReason()));
                context.getUsageTracker().add(effectiveResponse.getTokenUsage());

                GuardrailDecision outputDecision = guardrailEngine.evaluateOutput(legacyContext, effectiveResponse);
                if (!outputDecision.isAllowed())
                {
                    throw new GuardrailTripwireException("output", outputDecision.getReason());
                }

                if (!effectiveResponse.getContent().isBlank())
                {
                    appendToMemory(context, runHooks, new Message(Role.ASSISTANT, effectiveResponse.getContent()), runConfiguration.getSessionId(), memoryPolicy, true);
                    context.getItems().add(new ContextItem(ContextItemType.ASSISTANT_MESSAGE, effectiveResponse.getContent(), effectiveResponse.getStructuredOutput()));
                }

                if (effectiveResponse.getHandoffAgentId().isPresent())
                {
                    String toAgent = effectiveResponse.getHandoffAgentId().get();
                    Handoff handoff = new Handoff(context.getCurrentAgent().definition().getId(), toAgent, "model requested handoff");
                    if (!context.getCurrentAgent().definition().getHandoffAgentIds().contains(toAgent) || !handoffRouter.canRoute(handoff))
                    {
                        throw new HandoffDeniedException(handoff.getFromAgentId(), toAgent);
                    }

                    IAgent next = agentRegistry.get(toAgent)
                        .orElseThrow(() -> new IllegalStateException("Handoff agent not found: " + toAgent));
                    context.setCurrentAgent(next);
                    context.getItems().add(new ContextItem(ContextItemType.HANDOFF, toAgent, Map.of("from", handoff.getFromAgentId())));
                    emit(context, runHooks, RunEventType.HANDOFF_OCCURRED, Map.of("from", handoff.getFromAgentId(), "to", toAgent));
                    if (context.getCurrentAgent().definition().isResetInputAfterHandoff())
                    {
                        userInput = null;
                    }
                    continue;
                }

                if (!effectiveResponse.getToolCalls().isEmpty())
                {
                    appendToMemory(context, runHooks, new Message(Role.ASSISTANT, effectiveResponse.getContent(), effectiveResponse.getToolCalls()), runConfiguration.getSessionId(), memoryPolicy, true);
                    executeToolCallsInParallel(context, runHooks, effectiveResponse.getToolCalls(), runConfiguration, memoryPolicy);
                    if (context.getCurrentAgent().definition().isResetInputAfterToolCall())
                    {
                        userInput = null;
                    }
                    continue;
                }

                OutputValidationResult classValidation = classSchema == null
                    ? OutputValidationResult.ok()
                    : outputValidator.validate(effectiveResponse.getStructuredOutput(), classSchema);
                if (!classValidation.isValid())
                {
                    context.getItems().add(new ContextItem(ContextItemType.SYSTEM_EVENT, "Structured output invalid: " + classValidation.getReason(), Map.of()));
                    continue;
                }

                if (context.getCurrentAgent().definition().getOutputSchemaName() != null)
                {
                    OutputValidationResult validation = outputSchemaRegistry
                        .get(context.getCurrentAgent().definition().getOutputSchemaName())
                        .map(schema -> outputValidator.validate(effectiveResponse.getStructuredOutput(), schema))
                        .orElse(OutputValidationResult.fail("Output schema not found: " + context.getCurrentAgent().definition().getOutputSchemaName()));
                    if (!validation.isValid())
                    {
                        context.getItems().add(new ContextItem(ContextItemType.SYSTEM_EVENT, "Structured output invalid: " + validation.getReason(), Map.of()));
                        continue;
                    }
                }

                return finalizeResult(context, effectiveResponse.getContent(), runConfiguration, legacyContext, memoryPolicy);
            }

            throw new MaxTurnsExceededException(runConfiguration.getMaxTurns());
        }
        catch (RuntimeException error)
        {
            runSpan.annotate("error", error.getMessage());
            return handleError(context, runConfiguration, error, legacyContext, memoryPolicy);
        }
        finally
        {
            runSpan.close();
        }
    }

    private ILlmStreamListener streamListenerForRunner(RunnerContext context, IRunHooks runHooks, StreamCapture streamCapture)
    {
        return new ILlmStreamListener()
        {
            @Override
            public void onDelta(String delta)
            {
                streamCapture.content.append(delta);
                ExecutionContext legacyContext = new ExecutionContext(context.getCurrentAgent(), context.getMemory(), context.getUsageTracker());
                hookManager.onModelResponseDelta(legacyContext, delta);
                emit(context, runHooks, RunEventType.MODEL_RESPONSE_DELTA, Map.of("delta", delta));
            }

            @Override
            public void onToolCall(ToolCall toolCall)
            {
                if (toolCall != null)
                {
                    streamCapture.toolCalls.add(toolCall);
                }
            }

            @Override
            public void onReasoningDelta(String reasoningDelta)
            {
                if (reasoningDelta == null || reasoningDelta.isBlank())
                {
                    return;
                }
                streamCapture.reasoning.append(reasoningDelta);
                emit(context, runHooks, RunEventType.MODEL_REASONING_DELTA, Map.of("delta", reasoningDelta));
            }

            @Override
            public void onTokenUsage(TokenUsage tokenUsage)
            {
                if (tokenUsage != null)
                {
                    streamCapture.tokenUsage = tokenUsage;
                }
            }

            @Override
            public void onHandoff(String handoffAgentId)
            {
                streamCapture.handoffAgentId = handoffAgentId;
            }

            @Override
            public void onCompleted(LlmResponse response)
            {
                streamCapture.finalResponse = response;
            }
        };
    }

    private LlmResponse mergeWithStreamedResponse(LlmResponse response, StreamCapture streamCapture)
    {
        LlmResponse base = response != null ? response : streamCapture.finalResponse;
        if (base == null)
        {
            return new LlmResponse(streamCapture.content.toString(),
                List.copyOf(streamCapture.toolCalls),
                streamCapture.handoffAgentId,
                streamCapture.tokenUsage == null ? new TokenUsage(0, 0) : streamCapture.tokenUsage,
                "stop",
                streamCapture.reasoning.toString(),
                Map.of(),
                List.of());
        }

        String content = base.getContent();
        if ((content == null || content.isBlank()) && !streamCapture.content.isEmpty())
        {
            content = streamCapture.content.toString();
        }

        List<ToolCall> toolCalls = base.getToolCalls();
        if ((toolCalls == null || toolCalls.isEmpty()) && !streamCapture.toolCalls.isEmpty())
        {
            toolCalls = List.copyOf(streamCapture.toolCalls);
        }

        String handoffAgentId = base.getHandoffAgentId().orElse(streamCapture.handoffAgentId);

        TokenUsage tokenUsage = base.getTokenUsage();
        if (tokenUsage == null)
        {
            tokenUsage = streamCapture.tokenUsage;
        }
        else if (streamCapture.tokenUsage != null && tokenUsage.getTotalTokens() == 0 && streamCapture.tokenUsage.getTotalTokens() > 0)
        {
            tokenUsage = streamCapture.tokenUsage;
        }
        if (tokenUsage == null)
        {
            tokenUsage = new TokenUsage(0, 0);
        }

        String reasoningContent = base.getReasoningContent();
        if ((reasoningContent == null || reasoningContent.isBlank()) && !streamCapture.reasoning.isEmpty())
        {
            reasoningContent = streamCapture.reasoning.toString();
        }

        return new LlmResponse(content == null ? "" : content,
            toolCalls == null ? List.of() : toolCalls,
            handoffAgentId,
            tokenUsage,
            base.getFinishReason(),
            reasoningContent,
            base.getStructuredOutput(),
            base.getGeneratedAssets());
    }

    private static final class StreamCapture
    {
        private final StringBuilder content = new StringBuilder();
        private final StringBuilder reasoning = new StringBuilder();
        private final List<ToolCall> toolCalls = new ArrayList<>();
        private TokenUsage tokenUsage;
        private String handoffAgentId;
        private LlmResponse finalResponse;
    }

    /**
     * Performs handle error as part of AgentRunner runtime responsibilities.
     * @param context The context used by this operation.
     * @param config The config used by this operation.
     * @param error The error used by this operation.
     * @return The value produced by this operation.
     */
    private ContextResult handleError(RunnerContext context, RunConfiguration config, RuntimeException error, ExecutionContext legacyContext, MemoryLifecyclePolicy memoryPolicy)
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
            String errorMessage = error instanceof ModelInvocationException mie 
                ? mie.getFullMessage() 
                : error.getMessage();
            finalOutput = "Run failed: " + errorMessage;
        }

        context.getItems().add(new ContextItem(ContextItemType.SYSTEM_EVENT, finalOutput, Map.of("errorKind", kind.name())));

        if (kind == RunErrorKind.INPUT_GUARDRAIL || kind == RunErrorKind.OUTPUT_GUARDRAIL)
        {
            emit(context, new IRunHooks()
            {
            }, RunEventType.GUARDRAIL_BLOCKED, Map.of("kind", kind.name(), "message", error.getMessage()));
        }

        emit(context, new IRunHooks()
        {
        }, RunEventType.RUN_FAILED, Map.of("kind", kind.name(), "message", error.getMessage()));
        return finalizeResult(context, finalOutput, config, legacyContext, memoryPolicy);
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

    private void executeToolCallsInParallel(RunnerContext context, IRunHooks runHooks, List<ToolCall> toolCalls, RunConfiguration config, MemoryLifecyclePolicy memoryPolicy)
    {
        List<ToolCall> approvedCalls = new ArrayList<>();
        for (ToolCall call : toolCalls)
        {
            emit(context, runHooks, RunEventType.TOOL_CALL_REQUESTED, Map.of("tool", call.getToolName(), "arguments", call.getArguments()));
            if (context.getCurrentAgent().definition().isRequireToolApproval())
            {
                ToolApprovalDecision decision = toolApprovalPolicy.decide(
                    new ToolApprovalRequest(context.getCurrentAgent().definition().getId(), call.getToolName(), call.getArguments()));
                if (!decision.isApproved())
                {
                    context.getItems().add(new ContextItem(ContextItemType.SYSTEM_EVENT,
                        "Tool call denied: " + decision.getReason(), Map.of("tool", call.getToolName())));
                    continue;
                }
            }
            approvedCalls.add(call);
        }

        if (approvedCalls.isEmpty())
        {
            return;
        }

        ReentrantLock hookLock = new ReentrantLock();
        Map<Integer, ToolExecutionOutcome> outcomes = new HashMap<>();
        try (ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor())
        {
            ExecutorCompletionService<ToolExecutionOutcome> completionService = new ExecutorCompletionService<>(executor);
            List<Future<ToolExecutionOutcome>> futures = new ArrayList<>();
            for (int i = 0; i < approvedCalls.size(); i++)
            {
                ToolCall call = approvedCalls.get(i);
                futures.add(completionService.submit(runToolCall(i, call, hookLock)));
            }

            for (int i = 0; i < approvedCalls.size(); i++)
            {
                ToolExecutionOutcome outcome = completionService.take().get();
                outcomes.put(outcome.index(), outcome);
            }

            for (Future<ToolExecutionOutcome> future : futures)
            {
                if (!future.isDone())
                {
                    future.cancel(true);
                }
            }
        }
        catch (RejectedExecutionException ex)
        {
            throw new ToolExecutionFailureException("virtual-thread-executor", ex);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new ToolExecutionFailureException("interrupted", ex);
        }
        catch (Exception ex)
        {
            if (ex.getCause() instanceof ToolExecutionFailureException failure)
            {
                throw failure;
            }
            throw new ToolExecutionFailureException("tool-execution", ex);
        }

        for (int i = 0; i < approvedCalls.size(); i++)
        {
            ToolCall call = approvedCalls.get(i);
            ToolExecutionOutcome outcome = outcomes.get(i);
            if (outcome == null)
            {
                throw new IllegalStateException("Missing tool result for: " + call.getToolName());
            }
            appendToMemory(context, runHooks, new Message(Role.TOOL, outcome.result(), call.getToolCallId()), config.getSessionId(), memoryPolicy, true);
            context.getItems().add(new ContextItem(ContextItemType.TOOL_CALL, call.getToolName(), call.getArguments()));
            context.getItems().add(new ContextItem(ContextItemType.TOOL_RESULT, outcome.result(), Map.of("tool", call.getToolName())));
            emit(context, runHooks, RunEventType.TOOL_CALL_COMPLETED, Map.of("tool", call.getToolName(), "result", outcome.result()));
        }
    }

    private Callable<ToolExecutionOutcome> runToolCall(int index, ToolCall call, ReentrantLock hookLock)
    {
        return () ->
        {
            ITool tool = toolRegistry.get(call.getToolName())
                .orElseThrow(() -> new IllegalStateException("Tool not found: " + call.getToolName()));
            ITraceSpan span = traceProvider.startSpan("agent.tool_call");
            span.annotate("tool", call.getToolName());
            try
            {
                hookLock.lock();
                try
                {
                    hookManager.beforeTool(call.getToolName(), call.getArguments());
                }
                finally
                {
                    hookLock.unlock();
                }

                String result = tool.execute(call.getArguments());

                hookLock.lock();
                try
                {
                    hookManager.afterTool(call.getToolName(), result);
                }
                finally
                {
                    hookLock.unlock();
                }
                return new ToolExecutionOutcome(index, call, result);
            }
            catch (RuntimeException ex)
            {
                span.annotate("error", ex.getMessage());
                throw new ToolExecutionFailureException(call.getToolName(), ex);
            }
            finally
            {
                span.close();
            }
        };
    }

    private record ToolExecutionOutcome(int index, ToolCall call, String result)
    {
    }

    private RunnerContext contextForBootstrap(IAgent agent, IAgentMemory memory)
    {
        return new RunnerContext(agent, memory);
    }

    private void appendToMemory(RunnerContext context, IRunHooks hooks, Message message, String sessionId, MemoryLifecyclePolicy memoryPolicy, boolean emitEvent)
    {
        ITraceSpan span = traceProvider.startSpan("agent.memory.write");
        long started = System.nanoTime();
        try
        {
            context.getMemory().append(message);
            List<Message> normalized = memoryPolicy.onWrite(context.getCurrentAgent().definition().getId(), sessionId, context.getMemory().messages());
            if (normalized.size() != context.getMemory().messages().size())
            {
                rewriteMemory(context.getMemory(), normalized);
            }
            if (emitEvent)
            {
                emit(context, hooks, RunEventType.MEMORY_WRITE, Map.of("messages", normalized.size(), "durationMs", durationMs(started)));
            }
        }
        finally
        {
            span.close();
        }
    }

    private List<Message> readFromMemory(RunnerContext context, IRunHooks hooks, String sessionId, MemoryLifecyclePolicy memoryPolicy)
    {
        ITraceSpan span = traceProvider.startSpan("agent.memory.read");
        long started = System.nanoTime();
        try
        {
            List<Message> messages = memoryPolicy.onRead(context.getCurrentAgent().definition().getId(), sessionId, context.getMemory().messages());
            emit(context, hooks, RunEventType.MEMORY_READ, Map.of("messages", messages.size(), "durationMs", durationMs(started), "cacheHit", false));
            return messages;
        }
        finally
        {
            span.close();
        }
    }

    private long durationMs(long startedNs)
    {
        return Math.max(0, (System.nanoTime() - startedNs) / 1_000_000);
    }

    private void rewriteMemory(IAgentMemory memory, List<Message> messages)
    {
        memory.replaceAll(messages);
    }

    private static final class SnapshotAgentMemory implements IAgentMemory
    {
        private final List<Message> messages;

        private SnapshotAgentMemory(List<Message> messages)
        {
            this.messages = List.copyOf(messages);
        }

        @Override
        public List<Message> messages()
        {
            return messages;
        }

        @Override
        public void append(Message message)
        {
            throw new UnsupportedOperationException("Snapshot memory is read-only");
        }
    }
    private LlmResponse invokeModel(ILlmClient llmClient,
                                    LlmRequest request,
                                    RunConfiguration config,
                                    boolean streamModelResponse,
                                    ILlmStreamListener streamListener)
    {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= config.getRetryPolicy().getMaxAttempts(); attempt++)
        {
            ITraceSpan span = traceProvider.startSpan("agent.model_call");
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
    private void emit(RunnerContext context, IRunHooks hooks, RunEventType type, Map<String, Object> attributes)
    {
        RunEvent event = new RunEvent(type, attributes);
        context.getEvents().add(event);
        hooks.onEvent(event);
        eventPublisher.publish(event);
    }

    /**
     * Performs finalize result as part of AgentRunner runtime responsibilities.
     * @param context The context used by this operation.
     * @param finalOutput The final output used by this operation.
     * @param config The config used by this operation.
     * @return The value produced by this operation.
     */
    private ContextResult finalizeResult(RunnerContext context, String finalOutput, RunConfiguration config, ExecutionContext legacyContext, MemoryLifecyclePolicy memoryPolicy)
    {
        legacyContext.setAgent(context.getCurrentAgent());
        hookManager.afterRun(legacyContext);
        if (config.getSessionId() != null)
        {
            sessionStore.save(new Session(config.getSessionId(), readFromMemory(context, new IRunHooks()
        {
        }, config.getSessionId(), memoryPolicy)));
        }
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("finalAgent", context.getCurrentAgent().definition().getId());
        emit(context, new IRunHooks()
        {
        }, RunEventType.RUN_COMPLETED, attrs);
        return new ContextResult(
            finalOutput,
            context.getCurrentAgent().definition().getId(),
            context.getUsageTracker().snapshot(),
            context.getItems(),
            context.getEvents());
    }
}
