package stark.dataworks.coderaider.gundam.core.extensions;

import java.util.Objects;

/**
 * Helper for trimming oversized tool outputs before adding them to memory or run items.
 */
public final class ToolOutputTrimmer
{
    private ToolOutputTrimmer()
    {
    }

    public static String trimToChars(String output, int maxChars)
    {
        Objects.requireNonNull(output, "output");
        if (maxChars < 1)
        {
            throw new IllegalArgumentException("maxChars must be >= 1");
        }

        if (output.length() <= maxChars)
        {
            return output;
        }

        String suffix = "\n...[truncated]";
        if (maxChars <= suffix.length())
        {
            return output.substring(0, maxChars);
        }
        return output.substring(0, maxChars - suffix.length()) + suffix;
    }
}
