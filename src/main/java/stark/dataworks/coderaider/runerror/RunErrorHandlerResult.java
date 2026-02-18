package stark.dataworks.coderaider.runerror;

public class RunErrorHandlerResult {
    private final boolean handled;
    private final String finalOutput;

    private RunErrorHandlerResult(boolean handled, String finalOutput) {
        this.handled = handled;
        this.finalOutput = finalOutput;
    }

    public static RunErrorHandlerResult notHandled() {
        return new RunErrorHandlerResult(false, null);
    }

    public static RunErrorHandlerResult handled(String finalOutput) {
        return new RunErrorHandlerResult(true, finalOutput);
    }

    public boolean isHandled() {
        return handled;
    }

    public String getFinalOutput() {
        return finalOutput;
    }
}
