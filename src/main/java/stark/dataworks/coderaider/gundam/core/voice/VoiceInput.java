package stark.dataworks.coderaider.gundam.core.voice;

import java.util.Objects;

/**
 * Audio chunk input for voice workflows.
 */
public class VoiceInput
{
    private final byte[] pcm16;
    private final int sampleRateHz;

    public VoiceInput(byte[] pcm16, int sampleRateHz)
    {
        this.pcm16 = Objects.requireNonNull(pcm16, "pcm16").clone();
        if (sampleRateHz < 8000)
        {
            throw new IllegalArgumentException("sampleRateHz must be >= 8000");
        }
        this.sampleRateHz = sampleRateHz;
    }

    public byte[] getPcm16()
    {
        return pcm16.clone();
    }

    public int getSampleRateHz()
    {
        return sampleRateHz;
    }
}
