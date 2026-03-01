package stark.dataworks.coderaider.gundam.core.react;

/**
 * ReActPromptComposer centralizes prompt assembly for ReAct-style runs.
 */
public final class ReActPromptComposer
{
    private static final String DEFAULT_REACT_INSTRUCTIONS = """
        ReAct mode is enabled.
        You must iteratively decide your next action:
        1) Reason about the current state and goal.
        2) If a tool is needed, call exactly one most relevant tool with concrete arguments.
        3) Observe the tool result and continue until done.
        4) When the task is complete, return the final answer clearly.
        Keep reasoning concise and action-oriented.
        """;

    private ReActPromptComposer()
    {
    }

    /**
     * Appends ReAct instructions to a base system prompt when ReAct mode is enabled.
     * @param baseSystemPrompt Agent system prompt.
     * @param customInstructions Optional custom ReAct instructions.
     * @param reactEnabled Whether ReAct mode is enabled.
     * @return Prompt suitable for model input.
     */
    public static String compose(String baseSystemPrompt, String customInstructions, boolean reactEnabled)
    {
        if (!reactEnabled)
        {
            return baseSystemPrompt;
        }

        String normalizedBase = baseSystemPrompt == null ? "" : baseSystemPrompt;
        String instructions = (customInstructions == null || customInstructions.isBlank())
            ? DEFAULT_REACT_INSTRUCTIONS
            : customInstructions;

        return normalizedBase + System.lineSeparator() + System.lineSeparator() + instructions;
    }
}
