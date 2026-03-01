package stark.dataworks.coderaider.gundam.core.react;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReActPromptComposerTest
{
    @Test
    void shouldReturnBasePromptWhenDisabled()
    {
        String composed = ReActPromptComposer.compose("base", null, false);
        Assertions.assertEquals("base", composed);
    }

    @Test
    void shouldAppendDefaultInstructionsWhenEnabled()
    {
        String composed = ReActPromptComposer.compose("base", null, true);
        Assertions.assertTrue(composed.contains("base"));
        Assertions.assertTrue(composed.contains("ReAct mode is enabled"));
    }

    @Test
    void shouldAppendCustomInstructionsWhenProvided()
    {
        String composed = ReActPromptComposer.compose("base", "custom-react", true);
        Assertions.assertTrue(composed.endsWith("custom-react"));
    }
}
