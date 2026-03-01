package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

public class LocalShellTool extends AbstractBuiltinTool
{
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    private static final boolean WINDOWS = OS_NAME.contains("win");

    public LocalShellTool(ToolDefinition definition)
    {
        super(definition, ToolCategory.SHELL);
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        String cmd = String.valueOf(input.getOrDefault("command", "echo empty"));
        StringBuilder result = new StringBuilder();
        result.append("$ ").append(cmd).append("\n");
        try
        {
            ProcessBuilder builder = createProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String output = r.lines().collect(Collectors.joining("\n"));
                if (!output.isEmpty())
                {
                    result.append(output).append("\n");
                }
            }
            int exitCode = process.waitFor();
            result.append("EXIT=").append(exitCode);
            return result.toString();
        }
        catch (Exception e)
        {
            return "Shell error: " + e.getMessage();
        }
    }

    private ProcessBuilder createProcessBuilder(String cmd)
    {
        if (WINDOWS)
        {
            return new ProcessBuilder("cmd", "/c", cmd);
        }
        return new ProcessBuilder("bash", "-lc", cmd);
    }
}
