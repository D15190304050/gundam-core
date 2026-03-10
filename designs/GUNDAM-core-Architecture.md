# GUNDAM-core Project Architecture and Code Structure

## 1. Runtime Architecture (Current State)

```mermaid
graph TD
    A[AgentRunner] --> B[IAgentRegistry]
    A --> C[IToolRegistry]
    A --> D[IContextBuilder]
    A --> E[HookManager]
    A --> F[GuardrailEngine]
    A --> G[HandoffRouter]
    A --> H[ISessionStore]
    A --> I[ITraceProvider]
    A --> J[IToolApprovalPolicy]
    A --> K[OutputSchemaRegistry]
    A --> L[OutputValidator]
    A --> M[RunEventPublisher]

    A --> N[LlmClientRegistry]
    N --> O[ILlmClient]
    O --> P[LlmRequest]
    O --> Q[LlmResponse]

    A --> R[RunConfiguration]
    A --> S[AgentRunnerContext]
    S --> T[IAgentMemory]
    S --> U[TokenUsageTracker]
    S --> V[RunEvent timeline]

    A --> W[Tool execution loop]
    W --> X[ITool]
    W --> Y[ToolApprovalRequest/ToolApprovalDecision]
```

### Execution contract

`AgentRunner` is the orchestration center. Each run initializes a `AgentRunnerContext`, loads optional session history into memory, and iterates turn-by-turn until termination. A turn may end in one of three paths:

1. **Tool path**: model returns tool calls -> approval policy -> tool execution -> tool outputs are appended -> continue loop.
2. **Handoff path**: model returns `handoffAgentId` -> `HandoffRouter` validates allow-list/filter -> current agent switches -> continue loop.
3. **Finalization path**: model returns final content/structured output -> optional schema validation -> session persistence -> `ContextResult`.

## 2. Design Principles

```mermaid
graph TD
    A[Runtime kernel first] --> B[Provider-adapter abstraction]
    B --> C[Composable registries]
    C --> D[Policy-driven guardrails and retries]
    D --> E[Streaming observability]
```

### Practical interpretation in this codebase

- **Kernel first**: core modules define contracts and orchestration, not app logic.
- **Provider abstraction**: every model integration goes through `ILlmClient`; multi-provider routing is handled by `LlmClientRegistry`.
- **Composable registries**: agents/tools/schemas/processors are all registry-driven.
- **Policy-driven safety**: guardrails, retries, tool approvals, and error handlers are pluggable.
- **Streaming observability**: token/reasoning deltas, lifecycle events, and spans are first-class runtime outputs.

## 3. Key Components

```mermaid
graph TD
    A[AgentDefinition + IAgent] --> B[AgentRegistry]
    C[ToolDefinition + ITool] --> D[ToolRegistry]
    E[DefaultContextBuilder] --> F[Message history rendering]
    G[GuardrailEngine] --> H[GuardrailDecision]
    I[HandoffRouter] --> J[Handoff]
    K[RunErrorHandlers] --> L[IRunErrorHandler]
    M[RunEventPublisher] --> N[IRunHooks / listeners]
    O[OutputSchemaMapper] --> P[responseFormatJsonSchema]
```

### Core package map

- **runner**: runtime loop (`AgentRunner`) and run knobs (`RunConfiguration`)
- **agent**: declarative agent model + registry + chat facade
- **tool**: typed tools, runtime execution, built-ins (agent/workflow/computer/patch/etc.)
- **llmspi**: provider abstraction, request/response model, streaming listener, adapter implementations
- **memory/session/context**: message history management + session persistence boundaries
- **events/streaming/tracing**: runtime visibility and telemetry
- **workflow**: DAG runtime and processors
- **mcp**: MCP client manager and proxy tooling
- **rag/multimodal/realtime/voice**: advanced capability interfaces and selected implementations

## 4. Provider Agnostic Layer

```mermaid
graph TD
    A[ILlmClient] --> B[ModelScopeLlmClient]
    A --> C[OpenAiLlmClient]
    A --> D[GeminiLlmClient]
    A --> E[QwenLlmClient]
    A --> F[SeedLlmClient]
    A --> G[DeepSeekLlmClient]
    A --> H[OpenAiCompatibleLlmClient]
    A --> I[SpringAiChatClientLlmClient]

    J[LlmClientRegistry] --> A

    K[IMultimodalLlmClient] --> A
    K --> L[IImageGenerator]
    K --> M[IVideoGenerator]
    K --> N[IAudioGenerator]
```

### Why this matters

- Model vendors can be swapped without changing `AgentRunner` logic.
- Multi-provider runs (including handoff across providers) are enabled by agent-level provider selection + `LlmClientRegistry`.
- Streaming and non-streaming flows share the same normalized `LlmResponse` contract.

## 5. Extension Points

```mermaid
graph TD
    A[HookManager] --> B[IAgentHook]
    A --> C[IToolHook]
    D[GuardrailEngine] --> E[IInputGuardrail]
    D --> F[IOutputGuardrail]
    G[HandoffRouter] --> H[IHandoffFilter]
    I[RunErrorHandlers] --> J[IRunErrorHandler]
    K[MemoryLifecyclePolicy] --> L[Sliding/Summarizing/Composite]
    M[ToolOutputTrimmer] --> N[Large-output control]
```

### Extension strategy

GUNDAM-core favors interface boundaries over inheritance-heavy frameworks. Most customizations are injected through constructor/builder wiring:

- runtime hooks (`IRunHooks`, `IAgentHook`, `IToolHook`)
- policy objects (`RetryPolicy`, `IToolApprovalPolicy`, memory lifecycle policies)
- registries (agents/tools/output schemas/workflow processors)
- provider adapters (`ILlmClient`)
- backend abstractions (`ISessionStore`, `IAgentMemory`, context-service memory store)

## 6. Agent and Workflow Composition Patterns

```mermaid
graph TD
    A[Orchestrator Agent] -->|tool call| B[WorkflowTool]
    B --> C[WorkflowExecutor]
    C --> D[Vertex: AgentWorkflowProcessor]
    D -->|nested run| E[Specialist Agent]
    C --> F[Vertex: JoinFieldsWorkflowProcessor]
    F --> G[Workflow final String output]
    G --> A
```

### Composition rules

- **Agent as a tool**: `AgentTool` wraps an agent invocation so another agent can delegate using normal tool-calling.
- **Workflow as a tool**: `WorkflowTool` exposes a workflow DAG as an agent-callable tool.
- **Agent as a workflow step**: `AgentWorkflowProcessor` executes one vertex by delegating to an agent.
- **Workflow result contract**: workflow output is normalized to final `String` for immediate agent consumption.

## 7. Data Flow Details (Added)

### 7.1 Memory/session flow

1. `RunConfiguration.sessionId` is checked.
2. If present, `ISessionStore.load(sessionId)` hydrates prior messages into memory.
3. Each new message/tool output is appended to `IAgentMemory`.
4. Memory lifecycle policy can compact/summarize/isolate after appends.
5. Final memory snapshot is persisted back through `ISessionStore`.

### 7.2 Streaming flow

- `runStreamed(...)` wires an `ILlmStreamListener`.
- Delta callbacks publish `MODEL_RESPONSE_DELTA` and optional `MODEL_REASONING_DELTA`.
- Tool call fragments and token usage are captured and merged into final `LlmResponse`.
- The return value remains deterministic (`ContextResult`), so callers can choose streaming without changing completion handling.

### 7.3 Structured output flow

- Structured output may come from prompt schema or class schema (`OutputSchemaMapper`).
- `OutputValidator` checks the final structured payload.
- Failures add system timeline items and continue loop/recovery logic rather than returning invalid payloads.

## 8. Package-level Implementation Status (Added)

- **Mature/production-shaped**: runner, tool system, agent registry, handoff, guardrail, tracing/events, approvals, output schema validation.
- **Feature-complete but evolving**: workflow DAG runtime, MCP subsystem, RAG/vector store, computer/editor tools.
- **Scaffolding/partial**: realtime and voice pipeline interfaces, multimodal generation provider implementations.

This split mirrors the repository goal: fully usable core runtime with clear extension seams for progressively implemented modalities and transports.

## 9. Recommended Code Reading Order (Added)

For a comprehensive understanding, read the code in the following order. This sequence is designed to move from runtime control flow to extension surfaces, then to advanced subsystems.

### Phase 1: Understand the execution backbone first

1. `src/main/java/stark/dataworks/coderaider/genericagent/core/runner/AgentRunner.java`
   - Main orchestration loop (`run`, `runStreamed`, tool loop, handoff loop, finalization).
2. `src/main/java/stark/dataworks/coderaider/genericagent/core/runner/RunConfiguration.java`
   - Runtime knobs: turns, retries, memory/session, provider options, output settings.
3. `src/main/java/stark/dataworks/coderaider/genericagent/core/runner/AgentRunnerContext.java`
   - Mutable run state (events, timeline items, memory, usage, current agent).
4. `src/main/java/stark/dataworks/coderaider/genericagent/core/runner/IRunHooks.java`
   - External callback surface for observing run events.

### Phase 2: Learn agent and model contracts

5. `src/main/java/stark/dataworks/coderaider/genericagent/core/agent/AgentDefinition.java`
6. `src/main/java/stark/dataworks/coderaider/genericagent/core/agent/IAgent.java`
7. `src/main/java/stark/dataworks/coderaider/genericagent/core/agent/AgentRegistry.java`
8. `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/ILlmClient.java`
9. `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/LlmRequest.java`
10. `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/LlmResponse.java`
11. `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/LlmClientRegistry.java`
12. `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/LlmOptions.java`

### Phase 3: Understand tool architecture and built-ins

13. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/ITool.java`
14. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/ToolDefinition.java`
15. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/ToolRegistry.java`
16. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/builtin/FunctionTool.java`
17. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/builtin/AgentTool.java`
18. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/builtin/WorkflowTool.java`
19. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/builtin/ComputerTool.java`
20. `src/main/java/stark/dataworks/coderaider/genericagent/core/tool/builtin/ApplyPatchTool.java`

### Phase 4: Safety, policy, and resilience layers

21. `src/main/java/stark/dataworks/coderaider/genericagent/core/guardrail/GuardrailEngine.java`
22. `src/main/java/stark/dataworks/coderaider/genericagent/core/approval/IToolApprovalPolicy.java`
23. `src/main/java/stark/dataworks/coderaider/genericagent/core/policy/RetryPolicy.java`
24. `src/main/java/stark/dataworks/coderaider/genericagent/core/runerror/RunErrorHandlers.java`
25. `src/main/java/stark/dataworks/coderaider/genericagent/core/runerror/RunErrorKind.java`

### Phase 5: Context, memory, and session persistence

26. `src/main/java/stark/dataworks/coderaider/genericagent/core/context/DefaultContextBuilder.java`
27. `src/main/java/stark/dataworks/coderaider/genericagent/core/memory/IAgentMemory.java`
28. `src/main/java/stark/dataworks/coderaider/genericagent/core/memory/InMemoryAgentMemory.java`
29. `src/main/java/stark/dataworks/coderaider/genericagent/core/memory/context/ContextServiceAgentMemory.java`
30. `src/main/java/stark/dataworks/coderaider/genericagent/core/memory/policy/MemoryLifecyclePolicy.java`
31. `src/main/java/stark/dataworks/coderaider/genericagent/core/session/ISessionStore.java`
32. `src/main/java/stark/dataworks/coderaider/genericagent/core/session/InMemorySessionStore.java`

### Phase 6: Handoff, output schemas, and observability

33. `src/main/java/stark/dataworks/coderaider/genericagent/core/handoff/HandoffRouter.java`
34. `src/main/java/stark/dataworks/coderaider/genericagent/core/output/OutputSchemaMapper.java`
35. `src/main/java/stark/dataworks/coderaider/genericagent/core/output/OutputValidator.java`
36. `src/main/java/stark/dataworks/coderaider/genericagent/core/events/RunEvent.java`
37. `src/main/java/stark/dataworks/coderaider/genericagent/core/streaming/RunEventPublisher.java`
38. `src/main/java/stark/dataworks/coderaider/genericagent/core/tracing/DistributedTraceProvider.java`
39. `src/main/java/stark/dataworks/coderaider/genericagent/core/tracing/processor/TracingProcessors.java`

### Phase 7: Advanced runtime modules

40. `src/main/java/stark/dataworks/coderaider/genericagent/core/workflow/WorkflowDefinition.java`
41. `src/main/java/stark/dataworks/coderaider/genericagent/core/workflow/WorkflowExecutor.java`
42. `src/main/java/stark/dataworks/coderaider/genericagent/core/workflow/processor/AgentWorkflowProcessor.java`
43. `src/main/java/stark/dataworks/coderaider/genericagent/core/mcp/McpManager.java`
44. `src/main/java/stark/dataworks/coderaider/genericagent/core/mcp/IMcpServerClient.java`
45. `src/main/java/stark/dataworks/coderaider/genericagent/core/rag/RagService.java`
46. `src/main/java/stark/dataworks/coderaider/genericagent/core/rag/InMemoryVectorStore.java`
47. `src/main/java/stark/dataworks/coderaider/genericagent/core/multimodal/MessagePart.java`
48. `src/main/java/stark/dataworks/coderaider/genericagent/core/realtime/IRealtimeSession.java`
49. `src/main/java/stark/dataworks/coderaider/genericagent/core/voice/IVoicePipeline.java`

### Phase 8: Adapter-level implementation details (provider-specific)

50. Read adapter base/normalization code first:
   - `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/adapter/OpenAiCompatibleLlmClient.java`
   - `src/main/java/stark/dataworks/coderaider/genericagent/core/llmspi/adapter/OpenAiCompatibleResponseConverter.java`
51. Then read concrete providers:
   - `ModelScopeLlmClient`, `OpenAiLlmClient`, `GeminiLlmClient`, `QwenLlmClient`, `SeedLlmClient`, `DeepSeekLlmClient`, `SpringAiChatClientLlmClient`.

### Phase 9: Validate understanding through executable examples

Use `src/test/java/stark/dataworks/coderaider/genericagent/core/examples` in numeric order (`Example01` -> `Example28`).

- `Example01`–`Example07`: baseline loop, tools, MCP, handoff, reasoning streams.
- `Example08`–`Example14`: declarative JSON loading, structured output, tracing.
- `Example15`–`Example20`: multimodal and RAG flows.
- `Example21`–`Example28`: tracking, patch/computer tools, ReAct debugging, agent/workflow composition.

This progression mirrors the runtime dependency graph and provides a reliable path to full-system comprehension.
