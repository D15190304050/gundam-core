# OpenAI Agents SDK vs GUNDAM-core: feature comparison, current progress, and next steps

This document compares the current capabilities of **OpenAI Agents SDK** (reference implementation in `references/openai-agents-python-main`) and **GUNDAM-core`.

## Scope

- OpenAI side: Python SDK in `references/openai-agents-python-main`.
- GUNDAM-core side: Java implementation in `src/main/java` and executable examples in `src/test/java/.../examples`.

## Feature comparison matrix

| Capability | OpenAI Agents SDK | GUNDAM-core status | Notes |
|---|---|---|---|
| Core agent loop (model -> tools -> continue) | ✅ | ✅ | `AgentRunner` implements loop, retries, hooks, guardrails, handoff flow. |
| ReAct mode (reasoning + iterative action planning) | ✅ | ✅ | `AgentDefinition.reactEnabled` and `reactInstructions` augment system prompts for explicit ReAct loop behavior. |
| Streaming token events | ✅ | ✅ | `runStreamed` and run-event publication available. |
| Reasoning stream handling | ✅ | ✅ | Reasoning delta events exposed and consumed in examples. |
| Tool calling (local tools) | ✅ | ✅ | Mature in both projects. |
| MCP tool integration | ✅ | ✅ | Supports stdio, HTTP, and streamable-http MCP usage. |
| Handoffs / multi-agent orchestration | ✅ | ✅ | `HandoffRouter` + handoff examples implemented. |
| Guardrails | ✅ | ✅ | Input/output guardrail evaluation in runner flow. |
| Structured output | ✅ | ✅ | Prompt and class-schema based structured output supported. |
| Tracing / observability | ✅ | ✅ | Trace provider abstractions, distributed tracing, and processors exist. |
| Session persistence | ✅ | ✅ | Session store abstraction and in-memory implementation available. |
| Memory backend pluggability | ✅ | ✅ | Caller-provided `IAgentMemory` supported per run. |
| Context-service memory backend | ✅ | ✅ | `ContextServiceAgentMemory` with `IContextServiceMemoryStore` implemented. |
| Memory lifecycle policies | ✅ | ✅ | `MemoryLifecyclePolicy`, sliding window, summarization, composite policies. |
| Human-in-the-loop approvals | ✅ | ✅ | Tool approval policy and requests implemented. |
| Multi-provider handoffs | ✅ | ✅ | `LlmClientRegistry` supports multiple providers in single session. |
| RAG / Vector store | ✅ | ✅ | `RagService`, `InMemoryVectorStore`, `EmbeddingModel` implemented. |
| Tool output trimming | ✅ | ✅ | `ToolOutputTrimmer` extension implemented. |
| Handoff history filters | ✅ | ✅ | `HandoffHistoryFilters` utilities implemented. |
| Computer tool | ✅ | ✅ | `ComputerTool` with full interface (`IComputer`, `AbstractComputer`, `SimulatedComputer`), supports screenshot, click, double_click, scroll, type, wait, move, keypress, drag. |
| Voice pipeline | ✅ | 🟡 Partial | `IVoicePipeline` interface exists, full workflow not implemented. |
| Multimodal generation (audio/image/video) | ✅ | 🟡 Partial | Interfaces exist (`IAudioGenerator`, `IImageGenerator`, `IVideoGenerator`), no providers. |
| Agent tool state tracking | ✅ | ✅ | `AgentToolUseTracker` implemented with serialization/hydration support. |
| Editor (file diff/patch) | ✅ | ✅ | `ApplyPatchTool`, `IApplyPatchEditor`, `DiffApplier` implemented for file operations. |
| Realtime voice/webhook workflows | ✅ | ⚪ Partial | Interfaces exist (`IRealtimeClient`, `IRealtimeSession`), no full implementation. |
| Codex/experimental features | ✅ | ⚪ Not implemented | OpenAI has experimental codex module. |
| Error handling registry | ✅ | ✅ | `RunErrorHandlers`, `IRunErrorHandler`, `RunErrorKind` implemented. |
| Spring AI compatibility | ⚪ | ✅ | `SpringAiToolAdapters`, `SpringAiChatClientLlmClient` unique to GUNDAM. |

Legend: ✅ implemented, 🟡 partial/in progress, ⚪ not implemented.

## Progress summary

1. **Memory pluggability completed**: `RunConfiguration` accepts optional caller-provided `IAgentMemory`; `ContextServiceAgentMemory` with `IContextServiceMemoryStore` implemented.
2. **Memory lifecycle policies implemented**: `MemoryLifecyclePolicy` interface with `SlidingWindowMemoryPolicy`, `SummarizingMemoryPolicy`, and `CompositeMemoryLifecyclePolicy` for compaction/retention/isolation.
3. **Example streaming hooks consolidated**: Duplicated event listeners centralized via shared helpers for text-only, tool-lifecycle, and reasoning-aware streaming output.
4. **Multi-provider handoffs**: `LlmClientRegistry` enables cross-provider handoffs (Example19 demonstrates ModelScope + Seed/Doubao in single session).
5. **RAG foundation**: `RagService`, `InMemoryVectorStore`, `EmbeddingModel`, and `SimpleHashEmbeddingModel` implemented (Example20 demonstrates retrieval-augmented generation).
6. **Tool output trimming**: `ToolOutputTrimmer` extension matches OpenAI's `ToolOutputTrimmer` for reducing token usage from large tool outputs.
7. **Handoff history filters**: `HandoffHistoryFilters` utilities for filtering conversation history during agent handoffs.
8. **Distributed tracing**: `DistributedTraceProvider`, `DistributedTraceCollector`, and tracing processors (Example13, Example14) provide observability.
9. **Error handling registry**: `RunErrorHandlers`, `IRunErrorHandler`, and `RunErrorKind` provide typed error classification and handler dispatch.
10. **Spring AI compatibility**: `SpringAiToolAdapters` and `SpringAiChatClientLlmClient` enable Spring AI `@Tool` annotation compatibility.
11. **Multimodal interfaces**: `IAudioGenerator`, `IImageGenerator`, `IVideoGenerator`, `IMultimodalLlmClient` interfaces defined for future provider implementations.
12. **Realtime interfaces**: `IRealtimeClient`, `IRealtimeSession`, `IRealtimeEventListener`, and transport abstractions (SSE, webhook) defined.
13. **Agent tool state tracking**: `AgentToolUseTracker` implemented to track which tools each agent has used, with serialization/hydration support for session persistence.
14. **Apply patch editor**: `ApplyPatchTool`, `IApplyPatchEditor`, `ApplyPatchOperation`, `ApplyPatchResult`, and `DiffApplier` implemented for file diff/patch operations (create, update, delete).
15. **Computer tool completed**: Full `IComputer` interface with `AbstractComputer` base class and `SimulatedComputer` for testing. Supports all operations: screenshot, click, double_click, scroll, type, wait, move, keypress, drag.
16. **Existing strengths retained**: lifecycle hooks, retries, guardrails, tracing, handoffs, MCP, and structured output remain aligned with design goals.
17. **ReAct debug-fix workflow hardened**: Example24 now enforces strict streaming turn limits and a plan-only coordinator step to avoid long-running loops while still exercising investigator/fixer/reviewer collaboration.
18. **Complex ReAct debugging scenario added**: Example25 introduces a harder runtime-logic repair flow on `InvoiceSummaryEngine.java`, requiring coordinated source inspection, multi-defect patching, and runtime output verification (`TOTAL=102.6`).

## New examples demonstrating capabilities

The following examples have been added to demonstrate new features:

- **Example13TracingSingleTurnTest**: Demonstrates distributed tracing with `DistributedTraceProvider` collecting span events.
- **Example14TracingMultiRoundTest**: Multi-turn distributed tracing with span relationships.
- **Example15IntegralImageQuestionTest**: Multimodal image understanding with `IMultimodalLlmClient`.
- **Example16KiteImageDescriptionTest**: Image description generation.
- **Example17FlyingDragonTextToImageTest**: Text-to-image generation with `IImageGenerator`.
- **Example18FlyingCatStyleTransferTest**: Style transfer image generation.
- **Example19MultiRoundMultiProviderHandoffStreamingTest**: Cross-provider handoffs (ModelScope + Seed/Doubao) in single session.
- **Example20RagVectorStoreStreamingTest**: RAG with `RagService`, `InMemoryVectorStore`, and `SimpleHashEmbeddingModel`.
- **Example21ToolUseTrackerTest**: Demonstrates `AgentToolUseTracker` for tracking tool usage across agents.
- **Example22ApplyPatchToolTest**: Demonstrates `ApplyPatchTool` for file diff/patch operations.
- **Example23ComputerToolTest**: Demonstrates `ComputerTool` for browser/desktop automation.
- **Example24ReActAgentDebugFixTest**: Demonstrates a coordinator/investigator/fixer/reviewer multi-agent topology where each role follows ReAct loops to debug and patch a Java bug with cross-platform command guidance.
- **Example25ComplexReActDebugFixTest**: Demonstrates a harder ReAct debugging pipeline that fixes multiple logical defects (iteration, tax rate, rounding) and validates correctness by compiling and executing the target Java program.

## What's next (recommended roadmap)

### 1) Realtime workflow implementation (medium priority)

- Complete `IRealtimeClient` and `IRealtimeSession` implementations.
- Implement SSE and webhook transport adapters.
- Add voice-to-text and text-to-voice pipeline integration.

### 2) Multimodal provider implementations (medium priority)

- Implement providers for `IAudioGenerator`, `IImageGenerator`, `IVideoGenerator`.
- Integrate with `IMultimodalLlmClient` for multimodal message handling.
- Add examples for audio/image/video generation workflows.

### 3) Voice pipeline implementation (medium priority)

- Complete `IVoicePipeline` implementation.
- Add speech-to-text and text-to-speech integration.
- Support real-time audio streaming.

### 4) Codex/experimental features (lower priority)

- Evaluate OpenAI's experimental codex module for relevant patterns.
- Consider implementing codex tool interfaces if applicable to use cases.

### 5) Production hardening

- Concurrency tests for multi-session + multi-agent runs on non-memory backends.
- Fault-injection tests for network partitions/timeouts on context-service memory.
- Idempotency and exactly-once semantics for session persistence APIs.
- Performance benchmarks for memory lifecycle policies and RAG operations.

## New classes and packages

### Tracking Package (`stark.dataworks.coderaider.gundam.core.tracking`)

- `AgentToolUseTracker`: Tracks which tools each agent has used during a run. Supports serialization/hydration for session persistence.
- `ToolUseTrackerSerializer`: Utility methods for serializing and hydrating tracker state.

### Editor Package (`stark.dataworks.coderaider.gundam.core.editor`)

- `IApplyPatchEditor`: Interface for host-defined editors that apply diffs on disk.
- `ApplyPatchOperation`: Represents a single apply_patch editor operation (create_file, update_file, delete_file).
- `ApplyPatchResult`: Optional metadata returned by editor operations.
- `DiffApplier`: Utility for applying V4A diffs against text inputs.

### Computer Package (`stark.dataworks.coderaider.gundam.core.computer`)

- `IComputer`: Interface abstracting operations needed to control a computer or browser.
- `AbstractComputer`: Base implementation providing common functionality.
- `SimulatedComputer`: Simulated implementation for testing purposes.
- `Environment`: Enum for environment types (mac, windows, ubuntu, browser).
- `Button`: Enum for mouse button types.

### Tool Package Updates (`stark.dataworks.coderaider.gundam.core.tool.builtin`)

- `ApplyPatchTool`: Hosted tool for file mutations via unified diffs.
- `ComputerTool`: Updated with full implementation supporting all computer operations.

## References used

- `README.md`
- `designs/GUNDAM-core-Architecture.md`
- `references/openai-agents-python-main`
