package stark.dataworks.coderaider.genericagent.core.excalibur;

import java.util.List;

/**
 * Predefined Excalibur role profiles for software engineering workflows.
 */
public enum ExcaliburAgentRole
{
    INVESTIGATOR(
        "Excalibur Investigator",
        ExcaliburTraeToolNames.ALL,
        """
        Role focus:
        - Investigate the reported software issue before proposing code edits.
        - Read the relevant source, verifier output, and nearby tests/examples.
        - Produce a concise root-cause report with evidence and a concrete fixer checklist.
        - Do not claim success before verification evidence exists.
        """),
    FIXER(
        "Excalibur Fixer",
        List.of(
            ExcaliburTraeToolNames.STR_REPLACE_BASED_EDIT_TOOL,
            ExcaliburTraeToolNames.JSON_EDIT_TOOL,
            ExcaliburTraeToolNames.BASH,
            ExcaliburTraeToolNames.SEQUENTIAL_THINKING,
            ExcaliburTraeToolNames.TASK_DONE,
            "apply_patch"),
        """
        Role focus:
        - Implement the smallest safe patch that resolves the confirmed root causes.
        - Re-read files before editing if patch context may be stale.
        - Verify immediately after each patch and iterate only when evidence shows more work is needed.
        - Keep the final response concise and include the exact fix outcome.
        """),
    REVIEWER(
        "Excalibur Reviewer",
        ExcaliburTraeToolNames.ALL,
        """
        Role focus:
        - Validate the fix using runtime evidence.
        - Return PASS only when the requested behavior contract is satisfied.
        - If verification fails, explain which requirement is still broken and why.
        """),
    SUMMARIZER(
        "Excalibur Summarizer",
        ExcaliburTraeToolNames.ALL,
        """
        Role focus:
        - Summarize the debugging session clearly.
        - Report files changed, the essential code changes, and the verification outcome.
        - Keep the summary concise and structured for engineers.
        """);

    private final String defaultName;
    private final List<String> defaultToolNames;
    private final String rolePrompt;

    ExcaliburAgentRole(String defaultName, List<String> defaultToolNames, String rolePrompt)
    {
        this.defaultName = defaultName;
        this.defaultToolNames = defaultToolNames;
        this.rolePrompt = rolePrompt;
    }

    public String getDefaultName()
    {
        return defaultName;
    }

    public List<String> getDefaultToolNames()
    {
        return defaultToolNames;
    }

    public String getRolePrompt()
    {
        return rolePrompt;
    }
}
