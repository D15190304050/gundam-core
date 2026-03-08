package stark.dataworks.coderaider.gundam.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModelInvocationException to verify error message improvements.
 */
public class ModelInvocationExceptionTest
{
    @Test
    public void testGetRootCauseMessageWithNoCause()
    {
        // Arrange
        ModelInvocationException ex = new ModelInvocationException("Test message", null);

        // Act
        String rootCause = ex.getRootCauseMessage();

        // Assert
        assertEquals("Test message", rootCause);
    }

    @Test
    public void testGetRootCauseMessageWithSingleCause()
    {
        // Arrange
        Throwable cause = new IllegalStateException("Root cause error");
        ModelInvocationException ex = new ModelInvocationException("Model invocation failed", cause);

        // Act
        String rootCause = ex.getRootCauseMessage();

        // Assert
        assertEquals("Root cause error", rootCause);
    }

    @Test
    public void testGetRootCauseMessageWithNestedCauses()
    {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("The actual root cause");
        Throwable intermediateCause = new IllegalStateException("Intermediate error", rootCause);
        ModelInvocationException ex = new ModelInvocationException("Model invocation failed after retries", intermediateCause);

        // Act
        String result = ex.getRootCauseMessage();

        // Assert
        assertEquals("The actual root cause", result);
    }

    @Test
    public void testGetFullMessageWithNoCause()
    {
        // Arrange
        ModelInvocationException ex = new ModelInvocationException("Test message", null);

        // Act
        String fullMessage = ex.getFullMessage();

        // Assert
        assertEquals("Test message", fullMessage);
    }

    @Test
    public void testGetFullMessageWithSingleCause()
    {
        // Arrange
        Throwable cause = new IllegalStateException("Provider call failed with status=400, body={\"error\":\"Bad request\"}");
        ModelInvocationException ex = new ModelInvocationException("Model invocation failed after retries", cause);

        // Act
        String fullMessage = ex.getFullMessage();

        // Assert
        assertEquals("Model invocation failed after retries -> Provider call failed with status=400, body={\"error\":\"Bad request\"}", fullMessage);
    }

    @Test
    public void testGetFullMessageWithNestedCauses()
    {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("Invalid API key");
        Throwable intermediateCause = new IllegalStateException("Authentication failed", rootCause);
        ModelInvocationException ex = new ModelInvocationException("Model invocation failed after retries", intermediateCause);

        // Act
        String fullMessage = ex.getFullMessage();

        // Assert
        assertEquals("Model invocation failed after retries -> Authentication failed -> Invalid API key", fullMessage);
    }

    @Test
    public void testGetRootCauseMessageWithNullCauseMessage()
    {
        // Arrange
        Throwable cause = new IllegalStateException();
        ModelInvocationException ex = new ModelInvocationException("Model invocation failed", cause);

        // Act
        String rootCause = ex.getRootCauseMessage();

        // Assert - Should return the top-level message when cause has no message
        assertEquals("Model invocation failed", rootCause);
    }
}
