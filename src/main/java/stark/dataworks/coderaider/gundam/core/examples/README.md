# GUNDAM-core Examples

These examples are organized from basic to complex:

1. `Example01SingleSimpleAgent`
2. `Example02AgentWithTools`
3. `Example03AgentWithMcp`
4. `Example04MultiRoundSingleAgentWithToolsAndMcp`
5. `Example05AgentGroupWithHandoffs`

## Run from IDE
Run each class' `main` method.

## Run with Maven (generic)
You can use `exec:java` with `-Dexec.mainClass=<full-class-name>`.

Example:

```bash
mvn -q -DskipTests exec:java \
  -Dexec.mainClass=stark.dataworks.coderaider.gundam.core.examples.Example01SingleSimpleAgent \
  -Dexec.args="gpt-4.1-mini https://your-base-url YOUR_API_KEY 'hello'"
```

> Note: examples use placeholder/mocked LLM logic to stay provider-agnostic. Replace mock `ILlmClient` with a real adapter in application code.

## Provider adapter classes

OpenAI-compatible adapters are available in `llmspi/adapter`:

- `OpenAiLlmClient`
- `GeminiLlmClient`
- `QwenLlmClient`
- `SeedLlmClient`
- `DeepSeekLlmClient`
- `SpringAiChatClientLlmClient` (bridge to Spring AI `ChatClient`)

They normalize native responses into `LlmResponse` (`content`, `toolCalls`, `handoffAgentId`) and support both sync and stream invocation.
