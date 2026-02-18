package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
/**
 * Class LocalShellTool.
 */

public class LocalShellTool extends AbstractBuiltinTool
{
    /**
     * Creates a new LocalShellTool instance.
     */
    public LocalShellTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.SHELL);
    }

    /**
     * Executes execute.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String cmd = String.valueOf(input.getOrDefault("command", "echo empty"));
        try
        {
            Process process = new ProcessBuilder("bash", "-lc", cmd).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                return r.lines().collect(Collectors.joining("\n"));
            }
        }
        catch (Exception e)
        {
            return "Shell error: " + e.getMessage();
        }
    }
}
