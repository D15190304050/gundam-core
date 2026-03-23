package stark.dataworks.coderaider.genericagent.core.excalibur;

import java.util.List;

/**
 * Trae-compatible built-in tool names mirrored for Excalibur agent profiles.
 */
public final class ExcaliburTraeToolNames
{
    public static final String STR_REPLACE_BASED_EDIT_TOOL = "str_replace_based_edit_tool";
    public static final String SEQUENTIAL_THINKING = "sequentialthinking";
    public static final String JSON_EDIT_TOOL = "json_edit_tool";
    public static final String TASK_DONE = "task_done";
    public static final String BASH = "bash";

    public static final List<String> ALL = List.of(
        STR_REPLACE_BASED_EDIT_TOOL,
        SEQUENTIAL_THINKING,
        JSON_EDIT_TOOL,
        TASK_DONE,
        BASH);

    private ExcaliburTraeToolNames()
    {
    }
}
