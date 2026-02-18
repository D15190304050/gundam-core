package stark.dataworks.coderaider.gundam.core.errors;

public class HandoffDeniedException extends AgentsException {
    public HandoffDeniedException(String from, String to) {
        super("Handoff denied from " + from + " to " + to);
    }
}
