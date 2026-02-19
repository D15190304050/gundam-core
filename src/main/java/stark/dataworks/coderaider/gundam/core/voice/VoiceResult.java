package stark.dataworks.coderaider.gundam.core.voice;

import java.util.Objects;

/**
 * Result of processing a voice pipeline turn.
 */
public class VoiceResult
{
    private final String transcript;
    private final String responseText;
    private final byte[] responseAudio;

    public VoiceResult(String transcript, String responseText, byte[] responseAudio)
    {
        this.transcript = transcript == null ? "" : transcript;
        this.responseText = responseText == null ? "" : responseText;
        this.responseAudio = Objects.requireNonNull(responseAudio, "responseAudio").clone();
    }

    public String getTranscript()
    {
        return transcript;
    }

    public String getResponseText()
    {
        return responseText;
    }

    public byte[] getResponseAudio()
    {
        return responseAudio.clone();
    }
}
