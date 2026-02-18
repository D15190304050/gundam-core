package stark.dataworks.coderaider.gundam.core.result;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;

/**
 * RunResult implements normalized run result structures.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class RunResult
{

    /**
     * Internal state for final output; used while coordinating runtime behavior.
     */
    private final String finalOutput;

    /**
     * Internal state for final agent id; used while coordinating runtime behavior.
     */
    private final String finalAgentId;

    /**
     * Internal state for usage; used while coordinating runtime behavior.
     */
    private final TokenUsage usage;

    /**
     * Internal state for items; used while coordinating runtime behavior.
     */
    private final List<RunItem> items;

    /**
     * Internal state for events; used while coordinating runtime behavior.
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
    public RunResult(String finalOutput,
                     String finalAgentId,
                     TokenUsage usage,
                     List<RunItem> items,
                     List<RunEvent> events)
    {
        this.finalOutput = finalOutput == null ? "" : finalOutput;
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.items = Collections.unmodifiableList(Objects.requireNonNull(items, "items"));
        this.events = Collections.unmodifiableList(Objects.requireNonNull(events, "events"));
    }
}
