package stark.dataworks.coderaider.result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import stark.dataworks.coderaider.event.RunEvent;
import stark.dataworks.coderaider.metrics.TokenUsage;

public class RunResult {
    private final String finalOutput;
    private final String finalAgentId;
    private final TokenUsage usage;
    private final List<RunItem> items;
    private final List<RunEvent> events;

    public RunResult(String finalOutput,
                     String finalAgentId,
                     TokenUsage usage,
                     List<RunItem> items,
                     List<RunEvent> events) {
        this.finalOutput = finalOutput == null ? "" : finalOutput;
        this.finalAgentId = Objects.requireNonNull(finalAgentId, "finalAgentId");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.items = Collections.unmodifiableList(Objects.requireNonNull(items, "items"));
        this.events = Collections.unmodifiableList(Objects.requireNonNull(events, "events"));
    }

    public String getFinalOutput() {
        return finalOutput;
    }

    public String getFinalAgentId() {
        return finalAgentId;
    }

    public TokenUsage getUsage() {
        return usage;
    }

    public List<RunItem> getItems() {
        return items;
    }

    public List<RunEvent> getEvents() {
        return events;
    }
}
