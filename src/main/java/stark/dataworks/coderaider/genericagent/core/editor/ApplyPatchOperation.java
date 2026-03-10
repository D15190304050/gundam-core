package stark.dataworks.coderaider.genericagent.core.editor;

import lombok.Getter;

import stark.dataworks.coderaider.genericagent.core.runner.AgentRunnerContext;

/**
 * Represents a single apply_patch editor operation requested by the model.
 */
@Getter
public class ApplyPatchOperation
{
    public enum OperationType
    {
        CREATE_FILE,
        UPDATE_FILE,
        DELETE_FILE
    }

    private final OperationType type;
    private final String path;
    private final String diff;
    private final AgentRunnerContext runnerContext;

    public ApplyPatchOperation(OperationType type, String path, String diff)
    {
        this(type, path, diff, null);
    }

    public ApplyPatchOperation(OperationType type, String path, String diff, AgentRunnerContext runnerContext)
    {
        this.type = type;
        this.path = path;
        this.diff = diff;
        this.runnerContext = runnerContext;
    }

    public static ApplyPatchOperation createFile(String path, String diff)
    {
        return new ApplyPatchOperation(OperationType.CREATE_FILE, path, diff);
    }

    public static ApplyPatchOperation updateFile(String path, String diff)
    {
        return new ApplyPatchOperation(OperationType.UPDATE_FILE, path, diff);
    }

    public static ApplyPatchOperation deleteFile(String path)
    {
        return new ApplyPatchOperation(OperationType.DELETE_FILE, path, null);
    }
}
