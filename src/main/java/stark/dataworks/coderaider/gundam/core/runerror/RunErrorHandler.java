package stark.dataworks.coderaider.gundam.core.runerror;

public interface RunErrorHandler {
    RunErrorHandlerResult onError(RunErrorData errorData);
}
