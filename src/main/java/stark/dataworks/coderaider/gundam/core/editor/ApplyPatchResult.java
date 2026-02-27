package stark.dataworks.coderaider.gundam.core.editor;

import lombok.Getter;

/**
 * Optional metadata returned by editor operations.
 */
@Getter
public class ApplyPatchResult
{
    public enum Status
    {
        COMPLETED,
        FAILED
    }

    private final Status status;
    private final String output;

    public ApplyPatchResult()
    {
        this(Status.COMPLETED, null);
    }

    public ApplyPatchResult(Status status, String output)
    {
        this.status = status;
        this.output = output;
    }

    public static ApplyPatchResult completed(String output)
    {
        return new ApplyPatchResult(Status.COMPLETED, output);
    }

    public static ApplyPatchResult failed(String output)
    {
        return new ApplyPatchResult(Status.FAILED, output);
    }
}
