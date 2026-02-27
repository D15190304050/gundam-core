package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchOperation;
import stark.dataworks.coderaider.gundam.core.editor.ApplyPatchResult;
import stark.dataworks.coderaider.gundam.core.editor.IApplyPatchEditor;

class ApplyPatchToolTest
{
    @Test
    void testToolDefinition()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        assertEquals("apply_patch", tool.definition().getName());
        assertTrue(tool.definition().getDescription().contains("file"));
        assertFalse(tool.needsApproval());
    }

    @Test
    void testCreateFile()
    {
        TestEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "create_file",
                "path", "/test/file.txt",
                "diff", "+line1\n+line2"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("completed"));
        assertEquals(1, editor.getCreateCount());
    }

    @Test
    void testUpdateFile()
    {
        TestEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "update_file",
                "path", "/test/file.txt",
                "diff", "-old\n+new"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("completed"));
        assertEquals(1, editor.getUpdateCount());
    }

    @Test
    void testDeleteFile()
    {
        TestEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "delete_file",
                "path", "/test/file.txt"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("completed"));
        assertEquals(1, editor.getDeleteCount());
    }

    @Test
    void testMissingOperation()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        String result = tool.execute(Map.of());
        assertTrue(result.contains("failed"));
    }

    @Test
    void testMissingType()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "path", "/test/file.txt"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("failed"));
    }

    @Test
    void testMissingPath()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "create_file"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("failed"));
    }

    @Test
    void testMissingDiffForCreate()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "create_file",
                "path", "/test/file.txt"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("failed"));
    }

    @Test
    void testUnknownOperationType()
    {
        IApplyPatchEditor editor = new TestEditor();
        ApplyPatchTool tool = new ApplyPatchTool(editor);

        Map<String, Object> input = Map.of(
            "operation", Map.of(
                "type", "unknown",
                "path", "/test/file.txt"
            )
        );

        String result = tool.execute(input);
        assertTrue(result.contains("failed"));
    }

    @Test
    void testApplyDiffStaticMethod()
    {
        String original = "line1\nline2\nline3";
        String diff = " line1\n-line2\n+line2_new\n line3";
        String result = ApplyPatchTool.applyDiff(original, diff);
        assertEquals("line1\nline2_new\nline3", result);
    }

    @Test
    void testApplyCreateDiffStaticMethod()
    {
        String diff = "+line1\n+line2";
        String result = ApplyPatchTool.applyCreateDiff(diff);
        assertEquals("line1\nline2", result);
    }

    private static class TestEditor implements IApplyPatchEditor
    {
        private int createCount = 0;
        private int updateCount = 0;
        private int deleteCount = 0;

        @Override
        public ApplyPatchResult createFile(ApplyPatchOperation operation)
        {
            createCount++;
            return ApplyPatchResult.completed("Created " + operation.getPath());
        }

        @Override
        public ApplyPatchResult updateFile(ApplyPatchOperation operation)
        {
            updateCount++;
            return ApplyPatchResult.completed("Updated " + operation.getPath());
        }

        @Override
        public ApplyPatchResult deleteFile(ApplyPatchOperation operation)
        {
            deleteCount++;
            return ApplyPatchResult.completed("Deleted " + operation.getPath());
        }

        public int getCreateCount()
        {
            return createCount;
        }

        public int getUpdateCount()
        {
            return updateCount;
        }

        public int getDeleteCount()
        {
            return deleteCount;
        }
    }
}
