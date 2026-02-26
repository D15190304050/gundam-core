package stark.dataworks.coderaider.gundam.core.tracing;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-process distributed tracing provider that emits trace/span ids for each nested operation.
 */
public class DistributedTraceProvider implements ITraceProvider
{
    private final DistributedTraceCollector collector;
    private final ThreadLocal<TraceState> state = ThreadLocal.withInitial(TraceState::new);

    public DistributedTraceProvider(DistributedTraceCollector collector)
    {
        this.collector = collector;
    }

    @Override
    public ITraceSpan startSpan(String name)
    {
        TraceState traceState = state.get();
        String traceId = traceState.traceId == null ? newId("trace") : traceState.traceId;
        if (traceState.traceId == null)
        {
            traceState.traceId = traceId;
        }
        String spanId = newId("span");
        String parent = traceState.stack.isEmpty() ? null : traceState.stack.peekLast();
        traceState.stack.addLast(spanId);
        Instant startedAt = Instant.now();
        Map<String, String> attrs = new HashMap<>();

        return new ITraceSpan()
        {
            @Override
            public void annotate(String key, String value)
            {
                attrs.put(key, value);
            }

            @Override
            public void close()
            {
                Instant endedAt = Instant.now();
                collector.collect(new DistributedTraceEvent(traceId, spanId, parent, name, startedAt, endedAt, Map.copyOf(attrs)));
                if (!traceState.stack.isEmpty() && spanId.equals(traceState.stack.peekLast()))
                {
                    traceState.stack.removeLast();
                }
                else
                {
                    traceState.stack.remove(spanId);
                }
                if (traceState.stack.isEmpty())
                {
                    traceState.traceId = null;
                }
            }
        };
    }

    private static String newId(String prefix)
    {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private static final class TraceState
    {
        private String traceId;
        private final Deque<String> stack = new ArrayDeque<>();
    }
}
