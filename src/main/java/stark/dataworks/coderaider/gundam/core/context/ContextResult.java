package stark.dataworks.coderaider.gundam.core.context;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;

/**
 * RunResult implements normalized run result structures.
 */
@Getter
public class ContextResult
{

    /**
     * Fallback or final output produced by error handling/execution.
     */
    private final String finalOutput;

    /**
     * Final agent id.
     */
    private final String finalAgentId;

    /**
     * Token usage reported for this response.
     */
    private final TokenUsage usage;

    /**
     * Collected context items produced during execution.
     */
    private final List<ContextItem> items;

    /**
     * Streamed run events captured for this result.
     */
    private final List<RunEvent> events;

    /**
     * Performs run result as part of RunResult runtime responsibilities.
     * @param finalOutput The final output used by this operation.
     * @param finalAgentId The final agent id used by this operation.
     * @param usage The usage used by this operation.
     * @param items The items used by this operation.
     * @param events The events used by this operation.
     */
    public ContextResult(String finalOutput,
                         String finalAgentId,
                         TokenUsage usage,
                         List<ContextItem> items,
                         List<RunEvent> events)
    {
        this.finalOutput = finalOutput == null ? "" : finalOutput;
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.items = Collections.unmodifiableList(Objects.requireNonNull(items, "items"));
        this.events = Collections.unmodifiableList(Objects.requireNonNull(events, "events"));
    }
}
