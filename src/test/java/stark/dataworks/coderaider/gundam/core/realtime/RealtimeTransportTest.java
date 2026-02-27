package stark.dataworks.coderaider.gundam.core.realtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.realtime.transport.SseRealtimeEventTransport;

class RealtimeTransportTest
{
    @Test
    void publishesSsePayload()
    {
        SseRealtimeEventTransport transport = new SseRealtimeEventTransport();
        AtomicReference<String> payload = new AtomicReference<>("");
        transport.subscribe(payload::set);
        transport.publish(new RealtimeEvent(RealtimeEventType.MODEL_DELTA, Instant.now(), Map.of("delta", "hi")));
        assertTrue(payload.get().contains("event: model_delta"));
        assertTrue(payload.get().contains("delta"));
    }
}
