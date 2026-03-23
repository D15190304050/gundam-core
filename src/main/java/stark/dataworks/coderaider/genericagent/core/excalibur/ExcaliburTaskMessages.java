package stark.dataworks.coderaider.genericagent.core.excalibur;

/**
 * Message builders that mirror trae-agent task bootstrap semantics.
 */
public final class ExcaliburTaskMessages
{
    private ExcaliburTaskMessages()
    {
    }

    public static String buildInitialUserMessage(ExcaliburTaskRequest request)
    {
        StringBuilder message = new StringBuilder();
        message.append("[Project root path]:\n")
            .append(request.getProjectPath())
            .append("\n\n");
        if (!request.getIssue().isBlank())
        {
            message.append("[Problem statement]: We're currently solving the following issue within our repository. Here's the issue text:\n")
                .append(request.getIssue())
                .append("\n\n");
        }
        message.append("[Task]:\n")
            .append(request.getTask())
            .append("\n");
        if (request.getBaseCommit() != null && !request.getBaseCommit().isBlank())
        {
            message.append("\n[Base commit]:\n")
                .append(request.getBaseCommit())
                .append("\n");
        }
        if (request.isMustPatch())
        {
            message.append("\n[Patch requirement]:\nYou must finish with a non-empty source patch.\n");
        }
        if (request.getPatchPath() != null)
        {
            message.append("\n[Patch output path]:\n")
                .append(request.getPatchPath())
                .append("\n");
        }
        return message.toString();
    }

    public static String buildRoleSpecificInstructions(ExcaliburTaskRequest request, ExcaliburAgentRole role)
    {
        StringBuilder instructions = new StringBuilder()
            .append("Task bootstrap semantics (ported from trae-agent):\n")
            .append("- Treat the project root path above as authoritative.\n")
            .append("- Use the issue statement as the main bug-fix target when it is present.\n")
            .append("- Prefer reproducing the current failure before claiming a fix.\n")
            .append("- If a patch is required, finish only after producing a non-empty source change.\n");
        if (request.getBaseCommit() != null && !request.getBaseCommit().isBlank())
        {
            instructions.append("- Compare against base commit ")
                .append(request.getBaseCommit())
                .append(" when reviewing the final diff.\n");
        }
        if (role == ExcaliburAgentRole.SUMMARIZER)
        {
            instructions.append("- Summaries should mention whether a diff or patch was produced.\n");
        }
        return instructions.toString();
    }
}
