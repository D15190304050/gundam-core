# GUNDAM-core Project Architecture and Code Structure

## 1. Runtime Architecture (Current State)

```mermaid
graph TD
    subgraph "GUNDAM-core Runtime Kernel"
        A[AgentRunner]
        B[ILlmClient / IMultimodalLlmClient]
        C[ToolRegistry]
        D[AgentRegistry]
        E[ContextBuilder]
        F[Memory + SessionStore]
        G[GuardrailEngine]
        H[HandoffRouter]
        I[HookManager]
        J[RunErrorHandlers + RetryPolicy]
        K[Tracing + RunEventPublisher]
        L[OutputValidator]
        M[MCP Manager]
        N[ToolApprovalPolicy]

        A --> B
        A --> C
        A --> D
        A --> E
        A --> F
        A --> G
        A --> H
        A --> I
        A --> J
        A --> K
        A --> L
        A --> M
        A --> N
    end

    subgraph "Tracing Pipeline"
        O[ProcessorTraceProvider]
        P[ITracingProcessor]
        Q[TracingProcessors]
        R[SpanData / ToolSpanData / GenerationSpanData]
        S[TraceEvent]

        O --> P
        O --> Q
        P --> R
        P --> S
    end

    subgraph "Provider Adapter Layer"
        T[OpenAiLlmClient]
        U[GeminiLlmClient]
        V[QwenLlmClient]
        W[SeedLlmClient]
        X[DeepSeekLlmClient]
        Y[SpringAiChatClientLlmClient]
        Z[OpenAiCompatibleLlmClient]

        T -.-> B
        U -.-> B
        V -.-> B
        W -.-> B
        X -.-> B
        Y -.-> B
        Z -.-> B
    end

    subgraph "Contract & Extension Layers"
        AA[realtime: IRealtimeClient / IRealtimeSession]
        AB[voice: IVoicePipeline + contracts]
        AC[extensions: HandoffHistoryFilters + ToolOutputTrimmer]
        AD[multimodal: MessagePart / IImageGenerator etc.]
        AE[builtin tools: web/file/shell/computer/code/image/video/function]
        AF[mcp tools: HostedMcpTool]

        A --> AC
        B --> AA
        B --> AB
        B --> AD
    end
```

## 2. Design Principles

```mermaid
graph TD
    A[Declarative First] --> B[Runtime-Centric]
    B --> C[Strict Separation of Concerns]
    C --> D[Provider Agnostic]
```

## 3. Key Components

```mermaid
graph TD
    A[AgentRunner] --> B[ILlmClient]
    B --> C[ToolRegistry]
    C --> D[AgentRegistry]
    D --> E[ContextBuilder]
    E --> F[Memory + SessionStore]
    F --> G[GuardrailEngine]
    G --> H[HandoffRouter]
    H --> I[HookManager]
    I --> J[RunErrorHandlers + RetryPolicy]
    J --> K[Tracing + RunEventPublisher]
    K --> L[OutputValidator]
    L --> M[MCP Manager]
    M --> N[ToolApprovalPolicy]
```

## 4. Provider Agnostic Layer

```mermaid
graph TD
    A[OpenAiLlmClient] --> B[ILlmClient]
    C[GeminiLlmClient] --> B
    D[QwenLlmClient] --> B
    E[SeedLlmClient] --> B
    F[DeepSeekLlmClient] --> B
    G[SpringAiChatClientLlmClient] --> B
    H[OpenAiCompatibleLlmClient] --> B
```

## 5. Extension Points

```mermaid
graph TD
    A[AgentRunner] --> B[Contract & Extension Layers]
    B --> C[realtime: IRealtimeClient]
    B --> D[voice: IVoicePipeline]
    B --> E[extensions: HandoffHistoryFilters]
    B --> F[multimodal: MessagePart]
    B --> G[builtin tools: web/file/shell]
    B --> H[mcp tools: HostedMcpTool]
```