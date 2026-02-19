package stark.dataworks.coderaider.gundam.core.extensions;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;

class ExtensionsTest
{
    @Test
    void handoffHistoryFiltersShouldTrimAndFilter()
    {
        List<Message> messages = List.of(
            new Message(Role.USER, "u1"),
            new Message(Role.TOOL, "t1"),
            new Message(Role.ASSISTANT, "a1"));

        List<Message> withoutTools = HandoffHistoryFilters.removeToolMessages(messages);
        Assertions.assertEquals(2, withoutTools.size());
        Assertions.assertEquals(Role.USER, withoutTools.get(0).getRole());

        List<Message> lastOne = HandoffHistoryFilters.lastNMessages(messages, 1);
        Assertions.assertEquals("a1", lastOne.get(0).getContent());
    }

    @Test
    void toolOutputTrimmerShouldAppendTruncationMarker()
    {
        String out = ToolOutputTrimmer.trimToChars("abcdefghijklmnopqrstuvwxyz", 22);
        Assertions.assertTrue(out.endsWith("[truncated]"));
    }
}
