package stark.dataworks.coderaider.gundam.core.client;

import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import stark.dataworks.coderaider.gundam.core.agent.IAgent;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.IRunHooks;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;

/**
 * Spring-AI style chat facade for {@link AgentRunner}.
 */
@RequiredArgsConstructor
public class AgentChatClient
{
    private final AgentRunner runner;
    private final String defaultAgentId;
    private final RunConfiguration defaultRunConfiguration;
    private final IRunHooks defaultRunHooks;
    private final boolean streamByDefault;

    public static AgentChatClient create(AgentRunner runner, String defaultAgentId)
    {
        return new AgentChatClient(runner, defaultAgentId, RunConfiguration.defaults(), new IRunHooks()
        {
        }, true);
    }

    public PromptSpec prompt()
    {
        return new PromptSpec(this, defaultAgentId, defaultRunConfiguration, defaultRunHooks, streamByDefault, null, null);
    }

    private CallSpec execute(PromptSpec spec)
    {
        String agentId = spec.agentId == null || spec.agentId.isBlank() ? defaultAgentId : spec.agentId;
        IAgent agent = runner.getAgentRegistry().get(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
        ContextResult result = spec.stream
            ? runner.runStreamed(agent, spec.userInput, spec.runConfiguration, spec.runHooks, spec.outputType)
            : runner.run(agent, spec.userInput, spec.runConfiguration, spec.runHooks, spec.outputType);
        return new CallSpec(result);
    }

    @RequiredArgsConstructor
    public static class PromptSpec
    {
        private final AgentChatClient client;
        private final String agentId;
        private final RunConfiguration runConfiguration;
        private final IRunHooks runHooks;
        private final boolean stream;
        private final String userInput;
        private final Class<?> outputType;

        public PromptSpec agent(String value)
        {
            return new PromptSpec(client, value, runConfiguration, runHooks, stream, userInput, outputType);
        }

        public PromptSpec user(String value)
        {
            return new PromptSpec(client, agentId, runConfiguration, runHooks, stream, Objects.requireNonNull(value, "userInput"), outputType);
        }

        public PromptSpec stream(boolean value)
        {
            return new PromptSpec(client, agentId, runConfiguration, runHooks, value, userInput, outputType);
        }

        public PromptSpec outputType(Class<?> value)
        {
            return new PromptSpec(client, agentId, runConfiguration, runHooks, stream, userInput, value);
        }

        public PromptSpec runConfiguration(RunConfiguration value)
        {
            return new PromptSpec(client, agentId, Objects.requireNonNull(value, "runConfiguration"), runHooks, stream, userInput, outputType);
        }

        public PromptSpec runHooks(IRunHooks value)
        {
            return new PromptSpec(client, agentId, runConfiguration, Objects.requireNonNull(value, "runHooks"), stream, userInput, outputType);
        }

        public PromptSpec session(String sessionId)
        {
            RunConfiguration cfg = runConfiguration;
            RunConfiguration copy = new RunConfiguration(cfg.getMaxTurns(), sessionId, cfg.getTemperature(), cfg.getMaxOutputTokens(), cfg.getToolChoice(),
                cfg.getResponseFormat(), Map.copyOf(cfg.getProviderOptions()), cfg.getRetryPolicy(), cfg.getRunErrorHandlers());
            return new PromptSpec(client, agentId, copy, runHooks, stream, userInput, outputType);
        }

        public CallSpec call()
        {
            return client.execute(this);
        }
    }

    @RequiredArgsConstructor
    public static class CallSpec
    {
        private final ContextResult result;

        public String content()
        {
            return result.getFinalOutput();
        }

        public ContextResult contextResult()
        {
            return result;
        }
    }
}
