package stark.dataworks.coderaider.errors;

public class AgentsException extends RuntimeException {
    public AgentsException(String message) {
        super(message);
    }

    public AgentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
