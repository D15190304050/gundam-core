package stark.dataworks.coderaider.errors;

public class MaxTurnsExceededException extends AgentsException {
    public MaxTurnsExceededException(int maxTurns) {
        super("Max turns exceeded: " + maxTurns);
    }
}
