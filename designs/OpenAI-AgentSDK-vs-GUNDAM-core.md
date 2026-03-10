# OpenAI Agents SDK vs GUNDAM-core

This document compares the current capabilities of:

- **OpenAI Agents SDK** (reference source in `references/openai-agents-python-main`)
- **GUNDAM-core** (`generic-agent-core`, Java runtime in `src/main/java`)

## Scope

- OpenAI side: Python SDK architecture/features used as baseline reference.
- GUNDAM-core side: Java runtime kernel + executable streaming examples in `src/test/java/.../examples`.

## Feature comparison matrix

| Capability | OpenAI Agents SDK | GUNDAM-core status | Notes |
|---|---|---|---|
| Core agent loop (model -> tools -> continue) | ✅ | ✅ | `AgentRunner` owns turn orchestration, retries, hooks, guardrails, handoff. |
| Streaming token events | ✅ | ✅ | `runStreamed` + run event publication (`MODEL_RESPONSE_DELTA`, reasoning delta). |
| Tool calling (local tools) | ✅ | ✅ | Includes typed tool schemas and execution hooks. |
| MCP tool integration | ✅ | ✅ | stdio, HTTP, streamable-HTTP MCP clients and manager. |
| Agent handoff orchestration | ✅ | ✅ | `HandoffRouter`, allow-lists, and history filtering extensions. |
| Guardrails | ✅ | ✅ | Input/output guardrail engine with tripwire semantics. |
| Structured output validation | ✅ | ✅ | Prompt/class schema mapping + validation pipeline. |
| Session persistence | ✅ | ✅ | `ISessionStore` abstraction + in-memory implementation. |
| Memory backend pluggability | ✅ | ✅ | `IAgentMemory` + context-service memory store options. |
| Memory lifecycle policies | ✅ | ✅ | Sliding window, summarization, composite policies. |
| Retry/backoff policy | ✅ | ✅ | `RetryPolicy` integrated into model invocation path. |
| Error handler registry | ✅ | ✅ | `RunErrorHandlers`, `RunErrorKind`, typed dispatch. |
| Tracing/observability | ✅ | ✅ | Trace providers + processor pipeline + distributed collector. |
| Tool-use tracking | ✅ | ✅ | `AgentToolUseTracker` + serializer/hydration helpers. |
| Agent-as-tool | ✅ | ✅ | `AgentTool` supported and covered by examples. |
| Workflow-as-tool / DAG | ✅ | ✅ | `WorkflowTool`, `WorkflowExecutor`, processor registry, retries/failure strategy. |
| Computer automation tool | ✅ | ✅ | `ComputerTool` + `IComputer` abstraction. |
| Diff/patch editor tool | ✅ | ✅ | `ApplyPatchTool`, `DiffApplier`, `IApplyPatchEditor`. |
| RAG/vector-store support | ✅ | ✅ | `RagService`, in-memory vector store, embedding model contracts. |
| Multi-provider runtime in one session | ✅ | ✅ | `LlmClientRegistry` + agent-level provider selection. |
| Spring AI tool/chat compatibility | ⚪ | ✅ | `SpringAiToolAdapters`, `SpringAiChatClientLlmClient` (GUNDAM-specific advantage). |
| Multimodal generation providers | ✅ | 🟡 Partial | Interfaces are present; provider-side full implementations are still evolving. |
| Voice pipeline end-to-end | ✅ | 🟡 Partial | Interfaces/config objects present; complete runtime pipeline pending. |
| Realtime workflow/webhook transport | ✅ | 🟡 Partial | Realtime transport interfaces + SSE/webhook scaffolding exist. |
| Experimental Codex modules | ✅ | ⚪ Not implemented | Not in current GUNDAM-core roadmap focus. |

Legend: ✅ implemented, 🟡 partial, ⚪ not implemented.

## Current GUNDAM-core profile

### Context model clarification

- GUNDAM-core now uses **one runtime context class**: `AgentRunnerContext`.
- Lifecycle hooks and guardrails now take `AgentRunnerContext` directly.
- `AgentRunnerContext` centralizes both execution state (`currentAgent`, `memory`, `usageTracker`, `currentStep`) and run orchestration state (`events`, `items`, `turns`).
- Conversation payloads are now normalized on **`ContextItem` only** (the previous `model.Message` class has been removed).
- `ContextItem` now carries message-role semantics (`role`, `parts`, `toolCalls`, `toolCallId`) and item semantics (`type`, `metadata`) so runtime memory, context builders, and provider adapters share one contract.
- This removes the previous dual-class split and avoids context bridging/allocation inside `AgentRunner`.

### Strongly implemented today

1. Runtime orchestration kernel (`AgentRunner`) with streaming/non-streaming parity.
2. Tool and agent composition primitives (local tools, agent-as-tool, workflow-as-tool).
3. Safety/control plane (guardrails, approval policies, retries, typed error handlers).
4. Observability plane (events, tracing, token accounting, tool-use tracking).
5. Provider-agnostic model layer with multiple adapters.
6. Session/memory foundations including memory lifecycle policy support.

### Implemented with evolving breadth

- MCP ecosystem support (multiple transports, approvals, manager/proxy contracts).
- Workflow DAG runtime and processor ecosystem.
- RAG/vector foundations and multimodal message contracts.

### Partial/scaffolding areas

- Realtime full session workflow.
- Voice pipeline implementation.
- Provider-specific implementations for audio/image/video generation.

## Example coverage alignment

The examples package contains **28 streaming-oriented test cases** (`Example01` ~ `Example28`) demonstrating the maturity areas above, including:

- basic and multi-round agent loops,
- tools and MCP,
- handoffs and multi-provider runs,
- tracing,
- structured output,
- multimodal input/output,
- RAG,
- tool use tracking,
- patch/computer tools,
- ReAct-style debugging,
- agent/workflow composition patterns.

## Recommended next roadmap

1. **Realtime completion**: finalize `IRealtimeClient` / `IRealtimeSession` implementations and durable transport behaviors.
2. **Voice pipeline completion**: integrate STT/TTS providers and streaming audio lifecycle.
3. **Multimodal provider depth**: add concrete `IImageGenerator`, `IAudioGenerator`, `IVideoGenerator` adapters.
4. **Production hardening**: concurrency/fault-injection/performance benchmarking on non-memory backends.
5. **Capability governance**: introduce compatibility test suites that continuously compare target behavior against OpenAI Agents SDK semantics.

## References

- `README.md`
- `designs/GUNDAM-core-Architecture.md`
- `designs/` directory design notes
- `references/openai-agents-python-main`
