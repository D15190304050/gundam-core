# Generic Agent Core (GUNDAM-core)

`generic-agent-core` is a Java runtime kernel for building provider-agnostic AI agents with OpenAI-Agents-style execution semantics (multi-turn loop, tool calling, handoff, streaming, and guardrails).

This repository is intentionally kernel-focused:
- It provides execution primitives and extension points.
- It does **not** include business platform concerns such as auth, tenancy, web APIs, or billing.

---

## What this project is

- A **runtime engine** centered on `AgentRunner`
- A **provider abstraction layer** centered on `ILlmClient`
- A **composable orchestration kernel** for agents, tools, workflows, and handoffs
- A **streaming-first** execution model with event publication and tracing

## What this project is not

- Not just an SDK wrapper
- Not an application framework
- Not a ready-made end-user product

---

## Design goals

1. **Declarative-first**: agent/workflow definitions can be loaded from JSON (`AgentDefinition.fromJson`, `WorkflowDefinition.fromJson`).
2. **Provider-agnostic**: model invocation goes through `ILlmClient` / `LlmClientRegistry`.
3. **Runtime-centric**: the kernel owns turn orchestration, tool looping, guardrails, retries, and session/memory flow.
4. **Strict separation of concerns**: no business-domain coupling in core modules.
5. **Extensible by interfaces**: hooks, guardrails, memory backends, tool approval policies, tracing providers, realtime transports, etc.

---

## High-level architecture

### Runtime backbone

- `runner`: `AgentRunner`, `RunConfiguration`, `AgentRunnerContext`, `IRunHooks`
- `agent`: `IAgent`, `AgentDefinition`, `IAgentRegistry`, `AgentRegistry`, `AgentChatClient`
- `tool`: `ITool`, `ToolDefinition`, `ToolRegistry`, built-ins (`AgentTool`, `WorkflowTool`, `FunctionTool`, `ComputerTool`, `ApplyPatchTool`, ...)
- `llmspi`: `ILlmClient`, `LlmRequest`, `LlmResponse`, `LlmOptions`, adapters and stream listener
- `context` + `memory` + `session`: conversation assembly and persistence

### Control and safety

- `guardrail`: input/output guardrails and decision engine
- `approval`: human-in-the-loop style tool approval policy
- `policy`: retry policy
- `runerror`: typed error classification and handler dispatch

### Observability and streaming

- `events` + `streaming`: `RunEvent`, `RunEventType`, `RunEventPublisher`
- `tracing`: trace providers, spans, distributed collector, processor pipeline
- `tracking`: tool-use tracker and serializer

### Advanced capabilities

- `handoff`: inter-agent handoff routing/filtering
- `workflow`: DAG execution with processor registry, retries, and failure strategy
- `mcp`: stdio/http/streamable-http MCP clients + approvals
- `rag`: embedding model + vector store + retrieval service
- `multimodal`: message parts and generation contracts (image/audio/video interfaces)
- `realtime` + `voice`: interfaces and transport scaffolding for realtime/voice flows
- `editor`, `computer`, `oss`: host-integrated utility capabilities

---

## Provider adapters currently included

OpenAI-compatible and bridge clients in `src/main/java/.../llmspi/adapter`:

- `ModelScopeLlmClient`
- `OpenAiLlmClient`
- `GeminiLlmClient`
- `QwenLlmClient`
- `SeedLlmClient`
- `DeepSeekLlmClient`
- `OpenAiCompatibleLlmClient`
- `SpringAiChatClientLlmClient`

---

## End-to-end execution model (`AgentRunner`)

For each turn:
1. Build context from memory/history.
2. Run input guardrails.
3. Invoke model (sync or streaming).
4. Accumulate token usage and emit events.
5. If model requested tools:
   - approval check,
   - execute tool(s),
   - append tool outputs,
   - continue loop.
6. If handoff requested:
   - validate with router/filter,
   - switch `currentAgent`,
   - continue loop.
7. Validate structured output if configured.
8. Persist session state and return `ContextResult`.

Termination conditions:
- Final assistant output produced
- `maxTurns` exceeded
- Guardrail tripwire / denied handoff / unhandled runtime error

---

## Examples (streaming, real model calls)

Examples are in `src/test/java/stark/dataworks/coderaider/genericagent/core/examples`.

Current suite:

1. `Example01SingleSimpleAgentTest`
2. `Example02AgentWithToolsTest`
3. `Example03AgentWithMcpTest`
4. `Example04MultiRoundSingleAgentWithToolsAndMcpTest`
5. `Example05MultiRoundSingleAgentWithToolsAndStreamableHttpMcpTest`
6. `Example06AgentGroupWithHandoffsTest`
7. `Example07ReasoningStreamingTest`
8. `Example08AgentDefinitionFromJsonTest`
9. `Example09DocxSkillAnalysisStreamingTest`
10. `Example10StructuredOutputByClassTest`
11. `Example11StructuredOutputByPromptTest`
12. `Example12AgentRunnerBuilderTest`
13. `Example13TracingSingleTurnTest`
14. `Example14TracingMultiRoundTest`
15. `Example15IntegralImageQuestionTest`
16. `Example16KiteImageDescriptionTest`
17. `Example17FlyingDragonTextToImageTest`
18. `Example18FlyingCatStyleTransferTextToImageTest`
19. `Example19MultiRoundMultiProviderHandoffStreamingTest`
20. `Example20RagVectorStoreStreamingTest`
21. `Example21ToolUseTrackerTest`
22. `Example22ApplyPatchToolTest`
23. `Example23ComputerToolTest`
24. `Example24ReActAgentDebugFixTest`
25. `Example25ComplexReActDebugFixTest`
26. `Example26AgentAsToolTest`
27. `Example27WorkflowAsToolTest`
28. `Example28AgentInWorkflowThenCalledByAnotherAgentTest`

> Note: examples are designed for real LLM calls and streaming event output.

---

## Local setup

### Prerequisites

- JDK 21+
- Maven 3.9+
- Python 3 (for MCP server scripts when running MCP examples)

Environment variables (or `.env.local` used by tests):

- `MODEL_SCOPE_API_KEY`
- `VOLCENGINE_API_KEY` (needed for some multi-provider examples)

### Run tests/examples

```bash
# compile + run all tests
mvn test

# run only example tests
mvn -Dtest="stark.dataworks.coderaider.genericagent.core.examples.Example*Test" test

# run one example
mvn -Dtest=stark.dataworks.coderaider.genericagent.core.examples.Example01SingleSimpleAgentTest test
```

For MCP examples, start test MCP servers first:

```bash
python3 src/main/resources/mcp/simple_mcp_server_http.py 8765
python3 src/main/resources/mcp/simple_mcp_server_streamable_http.py 8766
```

---

## Design/reference docs

- `designs/GUNDAM-core-Architecture.md`
- `designs/OpenAI-AgentSDK-vs-GUNDAM-core.md`
- `designs/` (design discussions and implementation notes)
- `references/openai-agents-python-main` (OpenAI Agents SDK source used as comparison reference)

---

## Non-goals

This kernel intentionally excludes:
- HTTP API layer
- UI layer
- tenant/account/auth concerns
- billing and usage charging systems
- application-specific business workflows

These should live in outer platform layers built on top of this runtime.
