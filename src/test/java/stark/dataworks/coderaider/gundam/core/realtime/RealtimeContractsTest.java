package stark.dataworks.coderaider.gundam.core.realtime;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RealtimeContractsTest
{
    @Test
    void realtimeEventShouldUseDefaults()
    {
        RealtimeEvent event = new RealtimeEvent(RealtimeEventType.INPUT_RECEIVED, null, null);
        Assertions.assertNotNull(event.getAt());
        Assertions.assertEquals(Map.of(), event.getData());
    }

    @Test
    void realtimeSessionConfigShouldValidate()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RealtimeSessionConfig("", "alloy", 100));
        RealtimeSessionConfig config = new RealtimeSessionConfig("gpt-realtime", null, 256);
        Assertions.assertEquals("alloy", config.getVoice());
        Assertions.assertTrue(config.getMaxOutputTokens() > 0);
    }
}
