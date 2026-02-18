package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * LocalShellTool implements tool contracts, schema metadata, and executable tool registration.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
public class LocalShellTool extends AbstractBuiltinTool
{

    /**
     * Performs local shell tool as part of LocalShellTool runtime responsibilities.
     * @param definition The definition used by this operation.
     */
    public LocalShellTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.SHELL);
    }

    /**
     * Runs the primary execution flow, coordinating model/tool work and runtime policies.
     * @param input The input used by this operation.
     * @return The value produced by this operation.
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
