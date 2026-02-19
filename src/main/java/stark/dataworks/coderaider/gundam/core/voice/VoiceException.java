package stark.dataworks.coderaider.gundam.core.voice;

/**
 * Base runtime exception for voice pipeline failures.
 */
public class VoiceException extends RuntimeException
{
    public VoiceException(String message)
    {
        super(message);
    }

    public VoiceException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
