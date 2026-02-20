# GUNDAM-core Examples

These examples are organized from basic to complex, all with **real streaming output** using **ModelScope** with Qwen models:

1. `Example01SingleSimpleAgent` - Basic agent with streaming
2. `Example02AgentWithTools` - Agent with tools and streaming
3. `Example03AgentWithMcp` - Agent with MCP tools and streaming
4. `Example04MultiRoundSingleAgentWithToolsAndMcp` - Multi-round conversation with streaming
5. `Example05AgentGroupWithHandoffs` - Agent group with handoffs and streaming

## Prerequisites

All examples require a **ModelScope API key**. You can provide it via:
- Environment variable: `MODEL_SCOPE_API_KEY`
- Command-line argument (second argument)

Get your API key from: https://modelscope.cn/

## Run from IDE
Run each class' `main` method. Make sure to set the `MODEL_SCOPE_API_KEY` environment variable first.

## Run with Maven

```powershell
# Set your API key (PowerShell)
$env:MODEL_SCOPE_API_KEY="ms-xxx"

# Run Example01
& "D:\Software\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" -q -DskipTests exec:java `
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example01SingleSimpleAgent

# Run Example02 with custom city
& "D:\Software\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" -q -DskipTests exec:java `
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example02AgentWithTools `
  -Dexec.args="Qwen/Qwen3-4B $env:MODEL_SCOPE_API_KEY Beijing"

# Run Example03
& "D:\Software\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" -q -DskipTests exec:java `
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example03AgentWithMcp

# Run Example04
& "D:\Software\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" -q -DskipTests exec:java `
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example04MultiRoundSingleAgentWithToolsAndMcp

# Run Example05
& "D:\Software\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" -q -DskipTests exec:java `
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example05AgentGroupWithHandoffs
```

## Streaming Output

All examples use `runStreamed()` with real streaming from ModelScope's API. Output appears incrementally in the console as tokens are generated, demonstrating:

- **MODEL_RESPONSE_DELTA** - Text tokens streamed in real-time
- **TOOL_CALL_REQUESTED/COMPLETED** - Tool execution events
- **HANDOFF_OCCURRED** - Agent handoff events (Example05)

## Available Models

ModelScope supports various Qwen models. Default model used: `Qwen/Qwen3-4B`

You can specify other models like:
- `Qwen/Qwen3-8B`
- `Qwen/Qwen3-14B`
- `Qwen/Qwen2.5-72B-Instruct`

## Provider adapter classes

OpenAI-compatible adapters are available in `llmspi/adapter`:

- `ModelScopeLlmClient` - ModelScope API (used in examples)
- `OpenAiLlmClient` - OpenAI API
- `GeminiLlmClient` - Google Gemini
- `QwenLlmClient` - Alibaba DashScope
- `SeedLlmClient` - ByteDance Seed
- `DeepSeekLlmClient` - DeepSeek
- `SpringAiChatClientLlmClient` - Spring AI bridge

They normalize native responses into `LlmResponse` (`content`, `toolCalls`, `handoffAgentId`) and support both sync and stream invocation.
