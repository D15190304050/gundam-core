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
| Tool calling (local tools) | ✅ | ✅ | Includes typed tool schemas and execution hooks, now with real Bing web search and PDF text extraction built-ins (Bing integration currently uses JDK HttpClient + JSON parsing, no dedicated Bing SDK dependency). |
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

## Recent reliability improvements (ReAct debug examples)

- `ApplyPatchTool` now aligns with OpenAI hosted apply_patch validation/output semantics (`create_file|update_file|delete_file` + `{status,output}`), while also accepting flat/raw payload fallbacks to improve model-call robustness in function-call providers.
- ReAct default prompt instructions were tightened to prefer concise internal reasoning and short final summaries instead of verbose thought transcripts.
- `Example24`, `Example25`, and `Example33` debug-fix flows now use lower-turn/lower-token configs and stronger patch-call guidance to reduce wasted deliberation while preserving streaming behavior.
- `Example25` now uses a multi-agent ReAct topology (coordinator/investigator/fixer/reviewer), includes source snapshots, and logs thought/action/observation output and provides explicit behavior-contract bug hints to make root-cause diagnosis and runtime verification traceable.
- The same three examples now validate concise runtime and concise structured summaries (`Problem`/`Fix`/`Verification` style) to better enforce “understand + fix + verify + summarize” outcomes.
- Added the `stark.dataworks.coderaider.genericagent.core.excalibur` package, which ports Trae-agent-style software-engineering guidance into reusable Java agent profiles (investigator/fixer/reviewer/summarizer) with shared workspace-aware prompts and defaults.
- `StepByStepRunnerTest` now exercises the Excalibur profiles end-to-end for a two-file Python debugging task, replacing the previous platform-specific planner/executor wiring with a reusable general-purpose software-engineering agent setup.
- Excalibur now also registers Trae-compatible tool names (`bash`, `sequentialthinking`, `task_done`, `str_replace_based_edit_tool`, `json_edit_tool`) and mirrors Trae's absolute-path/tool-usage contract more closely so the Java runner can execute Trae-style prompts with much higher fidelity.

## Example coverage alignment

The examples package contains **33 streaming-oriented test cases** (`Example01` ~ `Example33`) demonstrating the maturity areas above, including:

- basic and multi-round agent loops,
- tools and MCP,
- handoffs and multi-provider runs,
- tracing,
- structured output,
- multimodal input/output,
- RAG,
- tool use tracking,
- patch/computer/pdf/web-search tools,
- ReAct-style debugging (including planner-first 4-agent mode),
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
