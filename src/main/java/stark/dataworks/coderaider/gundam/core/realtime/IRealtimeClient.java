package stark.dataworks.coderaider.gundam.core.realtime;

/**
 * SPI for providers that support realtime sessions.
 */
public interface IRealtimeClient
{
    IRealtimeSession open(RealtimeSessionConfig configuration);
}
