package stark.dataworks.coderaider.runerror;

public interface RunErrorHandler {
    RunErrorHandlerResult onError(RunErrorData errorData);
}
