package stark.dataworks.coderaider.gundam.core.realtime.transport;

import stark.dataworks.coderaider.gundam.core.realtime.RealtimeEvent;

/**
 * Transport-neutral event sink for realtime orchestration.
 */
public interface IRealtimeEventTransport
{
    void publish(RealtimeEvent event);
}
