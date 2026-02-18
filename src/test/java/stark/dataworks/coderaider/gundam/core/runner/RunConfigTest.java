package stark.dataworks.coderaider.gundam.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class RunConfigTest
{

    @Test
    void rejectsTemperatureOutsideSupportedRange()
    {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> new RunConfig(12, null, 2.5, 256, "auto", "text", Map.of()));

        assertEquals("temperature must be between 0 and 2", error.getMessage());
    }

    @Test
    void rejectsMaxTurnsBelowOne()
    {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> new RunConfig(0, null, 0.2, 256, "auto", "text", Map.of()));

        assertEquals("maxTurns must be >= 1", error.getMessage());
    }

    @Test
    void rejectsMaxOutputTokensBelowOne()
    {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> new RunConfig(12, null, 0.2, 0, "auto", "text", Map.of()));

        assertEquals("maxOutputTokens must be >= 1", error.getMessage());
    }
}
