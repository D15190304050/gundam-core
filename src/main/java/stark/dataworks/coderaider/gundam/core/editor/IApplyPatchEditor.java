package stark.dataworks.coderaider.gundam.core.editor;

/**
 * Host-defined editor that applies diffs on disk.
 * Implement this interface to provide custom file editing behavior for the apply_patch tool.
 */
public interface IApplyPatchEditor
{
    /**
     * Creates a new file with the specified content.
     * @param operation The operation containing path and diff content.
     * @return The result of the operation.
     */
    ApplyPatchResult createFile(ApplyPatchOperation operation);

    /**
     * Updates an existing file by applying the diff.
     * @param operation The operation containing path and diff content.
     * @return The result of the operation.
     */
    ApplyPatchResult updateFile(ApplyPatchOperation operation);

    /**
     * Deletes a file at the specified path.
     * @param operation The operation containing the path to delete.
     * @return The result of the operation.
     */
    ApplyPatchResult deleteFile(ApplyPatchOperation operation);
}
