package stark.dataworks.coderaider.tool.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import stark.dataworks.coderaider.tool.ToolCategory;
import stark.dataworks.coderaider.tool.ToolDefinition;

public class LocalShellTool extends AbstractBuiltinTool {
    public LocalShellTool(ToolDefinition definition) {
        super(definition, ToolCategory.SHELL);
    }

    @Override
    public String execute(Map<String, Object> input) {
        String cmd = String.valueOf(input.getOrDefault("command", "echo empty"));
        try {
            Process process = new ProcessBuilder("bash", "-lc", cmd).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return r.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return "Shell error: " + e.getMessage();
        }
    }
}
