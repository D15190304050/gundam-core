package stark.dataworks.coderaider.gundam.core.voice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VoiceContractsTest
{
    @Test
    void voiceInputAndResultShouldDefensivelyCopyArrays()
    {
        byte[] audio = new byte[]{1, 2, 3};
        VoiceInput input = new VoiceInput(audio, 16000);
        audio[0] = 9;
        Assertions.assertEquals(1, input.getPcm16()[0]);

        VoiceResult result = new VoiceResult("hi", "hello", new byte[]{4, 5});
        byte[] fromResult = result.getResponseAudio();
        fromResult[0] = 99;
        Assertions.assertEquals(4, result.getResponseAudio()[0]);
    }

    @Test
    void voicePipelineConfigShouldSetDefaults()
    {
        VoicePipelineConfig config = new VoicePipelineConfig("gpt-4o-realtime", null, null);
        Assertions.assertEquals("alloy", config.getVoice());
        Assertions.assertEquals("", config.getInstructions());
    }
}
