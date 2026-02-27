package stark.dataworks.coderaider.gundam.core.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiffApplierTest
{
    @Test
    void testApplyCreateDiff()
    {
        String diff = "+line1\n+line2\n+line3";
        String result = DiffApplier.applyDiff("", diff, DiffApplier.ApplyDiffMode.CREATE);
        assertEquals("line1\nline2\nline3", result);
    }

    @Test
    void testApplyCreateDiffWithGitFormat()
    {
        String diff = "diff --git a/test.txt b/test.txt\n" +
            "new file mode 100644\n" +
            "index 0000000..0000000\n" +
            "--- /dev/null\n" +
            "+++ b/test.txt\n" +
            "@@ -0,0 +1,3 @@\n" +
            "+line1\n" +
            "+line2\n" +
            "+line3";
        String result = DiffApplier.applyDiff("", diff, DiffApplier.ApplyDiffMode.CREATE);
        assertEquals("line1\nline2\nline3", result);
    }

    @Test
    void testApplyUpdateDiffSimple()
    {
        String original = "line1\nline2\nline3";
        String diff = " line1\n-line2\n+line2_modified\n line3";
        String result = DiffApplier.applyDiff(original, diff);
        assertEquals("line1\nline2_modified\nline3", result);
    }

    @Test
    void testApplyUpdateDiffWithAddition()
    {
        String original = "line1\nline2";
        String diff = " line1\n line2\n+line3";
        String result = DiffApplier.applyDiff(original, diff);
        assertEquals("line1\nline2\nline3", result);
    }

    @Test
    void testApplyUpdateDiffWithDeletion()
    {
        String original = "line1\nline2\nline3";
        String diff = " line1\n-line2\n line3";
        String result = DiffApplier.applyDiff(original, diff);
        assertEquals("line1\nline3", result);
    }

    @Test
    void testApplyDiffWithEmptyDiff()
    {
        String original = "line1\nline2";
        String result = DiffApplier.applyDiff(original, "");
        assertEquals(original, result);
    }

    @Test
    void testApplyDiffWithNullDiff()
    {
        String original = "line1\nline2";
        String result = DiffApplier.applyDiff(original, null);
        assertEquals(original, result);
    }

    @Test
    void testApplyDiffWithNullInput()
    {
        String diff = "+line1\n+line2";
        String result = DiffApplier.applyDiff(null, diff, DiffApplier.ApplyDiffMode.CREATE);
        assertEquals("line1\nline2", result);
    }

    @Test
    void testApplyDiffPreservesNewlines()
    {
        String original = "line1\r\nline2\r\nline3";
        String diff = " line1\r\n-line2\r\n+line2_new\r\n line3";
        String result = DiffApplier.applyDiff(original, diff);
        assertTrue(result.contains("\r\n"));
        assertEquals("line1\r\nline2_new\r\nline3", result);
    }

    @Test
    void testApplyDiffWithMultipleChanges()
    {
        String original = "a\nb\nc\nd\ne";
        String diff = " a\n-b\n+c_new\n c\n-d\n-e";
        String result = DiffApplier.applyDiff(original, diff);
        assertEquals("a\nc_new\nc", result);
    }

    @Test
    void testApplyDiffWithAnchor()
    {
        String original = "line1\nline2\nline3\n";
        String diff = "@@ line1\n-line2\n+updated\n line3";
        String result = DiffApplier.applyDiff(original, diff);
        assertEquals("line1\nupdated\nline3\n", result);
    }

    @Test
    void testApplyDiffWithBareAnchor()
    {
        String diff = "@@\n+hello\n+world";
        String result = DiffApplier.applyDiff("", diff);
        assertEquals("hello\nworld\n", result);
    }
}
