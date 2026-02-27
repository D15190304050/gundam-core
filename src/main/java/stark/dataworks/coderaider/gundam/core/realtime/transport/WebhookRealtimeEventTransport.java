package stark.dataworks.coderaider.gundam.core.realtime.transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import stark.dataworks.coderaider.gundam.core.realtime.RealtimeEvent;

/**
 * Webhook adapter for realtime events.
 */
public class WebhookRealtimeEventTransport implements IRealtimeEventTransport
{
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final URI endpoint;

    public WebhookRealtimeEventTransport(URI endpoint)
    {
        this.endpoint = endpoint;
    }

    @Override
    public void publish(RealtimeEvent event)
    {
        try
        {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        }
        catch (IOException | InterruptedException exception)
        {
            if (exception instanceof InterruptedException)
            {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to publish realtime webhook event", exception);
        }
    }
}
