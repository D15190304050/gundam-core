package stark.dataworks.coderaider.gundam.core.result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.event.RunEvent;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
/**
 * Class RunResult.
 */

public class RunResult
{
    /**
     * Field finalOutput.
     */
    private final String finalOutput;
    /**
     * Field finalAgentId.
     */
    private final String finalAgentId;
    /**
     * Field usage.
     */
    private final TokenUsage usage;
    /**
     * Field items.
     */
    private final List<RunItem> items;
    /**
     * Field events.
     */
    private final List<RunEvent> events;
    /**
     * Creates a new RunResult instance.
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
    /**
     * Executes getFinalOutput.
     */

    public String getFinalOutput()
    {
        return finalOutput;
    }
    /**
     * Executes getFinalAgentId.
     */

    public String getFinalAgentId()
    {
        return finalAgentId;
    }
    /**
     * Executes getUsage.
     */

    public TokenUsage getUsage()
    {
        return usage;
    }
    /**
     * Executes getItems.
     */

    public List<RunItem> getItems()
    {
        return items;
    }
    /**
     * Executes getEvents.
     */

    public List<RunEvent> getEvents()
    {
        return events;
    }
}
