package stark.dataworks.coderaider.genericagent.core.react;

public final class ReActPromptComposer
{
    private static final String DEFAULT_REACT_INSTRUCTIONS = """
        ReAct mode is enabled.

        Workflow:
        1) Think briefly and internally.
        2) Use tools immediately when needed.
        3) Stop once verification is successful.

        Tool call format:
        - Preferred: {"type":"update_file","path":"...","diff":"..."}
        - Also accepted: {"operation":{"type":"update_file","path":"...","diff":"..."}}

        Final answer format:
        ## Summary
        - Problem: <issue>
        - Fix: <what changed>
        - Verification: <command + result>

        Rules:
        - Keep responses concise.
        - Do not emit long Thought/Action/Observation transcripts.
        """;

    private static final String INTENT_RECOGNITION_PREFIX = """
        [Intent]
        Task: <goal>
        Files: <paths>
        Tools: <names>
        
        """;

    private ReActPromptComposer()
    {
    }

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

        return INTENT_RECOGNITION_PREFIX + normalizedBase + System.lineSeparator() + System.lineSeparator() + instructions;
    }
}
