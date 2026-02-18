# GUNDAM-core

> **Generic Ultimate Neural-linked Data-driven AI Model Core**

GUNDAM-core is a modular, provider-agnostic, declarative-first Agent Runtime Kernel.

It is not a demo framework.  
It is not a wrapper around an API.  
It is not a business platform.

It is the engine.

---

# Vision

GUNDAM-core is designed to be:

- A runtime kernel for LLM-based Agents
- 100% feature-compatible with OpenAI's Agent SDK
- Provider-agnostic (OpenAI, Gemini, Qwen, LiteLLM, etc.)
- Declarative-first (JSON-defined Agents)
- Extensible via runtime hooks
- Business-logic free
- Evolvable into a full Generic Agent Platform

It is built to power:

- Dualwalker
- Future Agent Store
- Future Agent Runner
- Any internal or external system that needs programmable AI agents

---

# Design Principles

## 1. Declarative First

Agents should be defined as JSON configurations.

Changing an Agent should not require code changes.
Agent definitions must be database-storable and runtime-reloadable.

Code-defined Agents are allowed, but considered static.

---

## 2. Runtime-Centric

This project is not an SDK wrapper.

It is a runtime engine:

- Step execution loop
- Tool call resolution
- Memory update
- Context rendering
- Token tracking
- Hook injection

---

## 3. Strict Separation of Concerns

GUNDAM-core MUST NOT include:

- Tenant logic
- Permission logic
- Authentication
- Database layer
- Business orchestration
- UI logic

Those belong to outer systems.

---

## 4. Provider Agnostic

Model access must go through an abstraction layer:

`ILlmClient`

No vendor lock-in.

---

## 5. Extensible by Hooks

Two categories of hooks are required:

- Agent Runtime Hooks
- Tool Execution Hooks

Hook implementations are not part of the kernel.
Only hook interfaces and lifecycle triggers are included.

---

# Feature Parity Target

GUNDAM-core must be feature-complete compared to openai-agents-python.

Including:

- Agent abstraction
- Tool definition and execution
- Step loop runtime
- Conversation memory
- Model abstraction
- Agent-to-Agent handoff
- Tool schema definition
- Generation options
- Message abstraction

Plus:

- Hook system
- Token usage tracking
- Multi-provider architecture
- Declarative agent definitions

---

# Module Structure

```
gundam-core
│
├── agent
│   ├── IAgent
│   ├── Agent
│   ├── AgentDefinition
│   ├── IAgentRegistry
│   └── AgentRegistry
│
├── runtime
│   ├── IAgentRunner
│   ├── AgentRunner
│   ├── IStepEngine
│   ├── DefaultStepEngine
│   └── ExecutionContext
│
├── tool
│   ├── ITool
│   ├── ToolDefinition
│   ├── ToolParameterSchema
│   ├── IToolRegistry
│   └── ToolRegistry
│
├── llm-spi
│   ├── ILlmClient
│   ├── LlmRequest
│   ├── LlmResponse
│   └── LlmOptions
│
├── memory
│   ├── IAgentMemory
│   └── InMemoryAgentMemory
│
├── context
│   ├── IContextBuilder
│   └── DefaultContextBuilder
│
├── model
│   ├── Message
│   └── Role
│
├── hook
│   ├── IAgentHook
│   ├── IToolHook
│   └── HookManager
│
├── metrics
│   ├── TokenUsage
│   └── TokenUsageTracker
│
└── adapters
    ├── openai
    ├── gemini
    └── qwen
```

---

# Core Runtime Loop

Pseudocode:

```
while not finished:
    render context
    call model
    track token usage

    if model requests tool:
        execute tool
        append tool result to memory
    else:
        return final answer
```

Termination conditions:

- Model produces final message
- Max steps reached
- Explicit stop condition

---

# Core Interfaces (Conceptual)

## IAgent

Represents a runtime agent instance.

## IAgentRunner

Responsible for executing an agent session.

## IStepEngine

Implements the execution loop.

## ITool

Represents a callable tool.

## ILlmClient

Provider-agnostic LLM interface:

```
chat(request: LlmRequest) -> LlmResponse
```

## IAgentMemory

Conversation state manager.

## IContextBuilder

Responsible for building model input context.

## IAgentHook

Agent lifecycle hook interface.

## IToolHook

Tool lifecycle hook interface.

---

# Non-Goals

This project will NOT include:

- HTTP server
- REST API
- Web UI
- Multi-tenant logic
- Database integration
- Billing system
- Authentication
- Authorization

Those belong to outer platform layers.

---

# Final Statement

GUNDAM-core is not a wrapper.

It is not a convenience layer.

It is the runtime engine for declarative, extensible, provider-agnostic AI agents.

It is the kernel.

---

# Current Implementation Status (Java Kernel)

The Java kernel now includes the following runtime capabilities aligned with OpenAI Agents SDK concepts:

- Agent + declarative agent definition loading from JSON
- Agent registry and runtime agent switching
- Step/turn execution runtime with max-turn stopping
- Provider-agnostic LLM SPI and configurable generation options
- Tool abstractions, tool registries, and JSON-schema conversion helpers
- Tool call execution loop and tool lifecycle hooks
- Agent lifecycle hooks
- Agent-to-agent handoff with handoff allow-list + handoff filters/router
- In-memory conversation memory
- Session persistence abstraction (`SessionStore`) and in-memory implementation
- Input and output guardrails
- Run result model with timeline events and items
- Token usage tracking
- Tracing abstractions (`TraceProvider`) with no-op implementation
- Structured output validation via output schemas
- Tool approval policy for human-in-the-loop style gating
- Retry policy for resilient model invocation
- Event publisher/listener for streaming-like run observability
- Built-in tool ecosystem stubs (web/file/shell/code/image/function)
- MCP subsystem foundation (server config/client/manager + proxy tools + resources/templates)
- Rich run error handling (typed exceptions + handler registry)
- Tracing processor pipeline (custom processors + emitted trace events)

This repository is designed as a runtime kernel; providers/adapters can plug in through `ILlmClient`, `SessionStore`, tracing providers, and tool implementations.
