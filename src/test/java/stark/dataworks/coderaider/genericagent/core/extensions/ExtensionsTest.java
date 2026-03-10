package stark.dataworks.coderaider.genericagent.core.extensions;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.model.Role;

class ExtensionsTest
{
    @Test
    void handoffHistoryFiltersShouldTrimAndFilter()
    {
        List<ContextItem> messages = List.of(
            new ContextItem(Role.USER, "u1"),
            new ContextItem(Role.TOOL, "t1"),
            new ContextItem(Role.ASSISTANT, "a1"));

        List<ContextItem> withoutTools = HandoffHistoryFilters.removeToolMessages(messages);
        Assertions.assertEquals(2, withoutTools.size());
        Assertions.assertEquals(Role.USER, withoutTools.get(0).getRole());

        List<ContextItem> lastOne = HandoffHistoryFilters.lastNMessages(messages, 1);
        Assertions.assertEquals("a1", lastOne.get(0).getContent());
    }

    @Test
    void toolOutputTrimmerShouldAppendTruncationMarker()
    {
        String out = ToolOutputTrimmer.trimToChars("abcdefghijklmnopqrstuvwxyz", 22);
        Assertions.assertTrue(out.endsWith("[truncated]"));
    }
}
