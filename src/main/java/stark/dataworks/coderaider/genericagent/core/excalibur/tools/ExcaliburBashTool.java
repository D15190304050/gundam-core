package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import stark.dataworks.coderaider.genericagent.core.tool.ToolCategory;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.AbstractBuiltinTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trae-compatible bash tool with a persistent shell session on Unix-like platforms.
 */
public final class ExcaliburBashTool extends AbstractBuiltinTool
{
    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private final Path workingDirectory;
    private Process persistentProcess;
    private BufferedWriter persistentStdin;
    private BufferedReader persistentStdout;

    public ExcaliburBashTool(Path workingDirectory)
    {
        super(new ToolDefinition(
            "bash",
            "Execute shell commands in the project workspace using a shared shell session. Supports Trae-compatible `command` and `restart` fields.",
            List.of(
                new ToolParameterSchema("command", "string", true, "Shell command to execute."),
                new ToolParameterSchema("restart", "boolean", false, "Restart the persistent shell session before executing the command."))),
            ToolCategory.SHELL);
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
    }

    @Override
    public synchronized String execute(Map<String, Object> input)
    {
        String command = String.valueOf(input.getOrDefault("command", "echo empty"));
        boolean restart = booleanValue(input.get("restart"));
        StringBuilder result = new StringBuilder();
        result.append("$ ").append(command).append("\n");
        try
        {
            if (WINDOWS)
            {
                if (restart)
                {
                    return "tool has been restarted.";
                }
                return result.append(executeStateless(command)).toString();
            }
            if (restart)
            {
                resetPersistentShell();
                ensurePersistentShell();
                return "tool has been restarted.";
            }
            ensurePersistentShell();
            String marker = "__EXCALIBUR_EXIT__" + System.nanoTime() + "__";
            persistentStdin.write(command);
            persistentStdin.newLine();
            persistentStdin.write("printf '" + marker + "%s\\n' $? ");
            persistentStdin.newLine();
            persistentStdin.flush();

            String line;
            while ((line = persistentStdout.readLine()) != null)
            {
                if (line.startsWith(marker))
                {
                    result.append("EXIT=").append(line.substring(marker.length()));
                    return result.toString();
                }
                result.append(line).append("\n");
            }
            resetPersistentShell();
            return result.append("EXIT=1\nShell error: persistent session ended unexpectedly").toString();
        }
        catch (Exception ex)
        {
            resetPersistentShell();
            return result.append("Shell error: ").append(ex.getMessage()).toString();
        }
    }

    private void ensurePersistentShell() throws IOException
    {
        if (persistentProcess != null && persistentProcess.isAlive())
        {
            return;
        }
        ProcessBuilder builder = new ProcessBuilder("bash", "--noprofile", "--norc");
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);
        persistentProcess = builder.start();
        persistentStdin = new BufferedWriter(new OutputStreamWriter(persistentProcess.getOutputStream(), StandardCharsets.UTF_8));
        persistentStdout = new BufferedReader(new InputStreamReader(persistentProcess.getInputStream(), StandardCharsets.UTF_8));
    }

    private String executeStateless(String command) throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder("cmd", "/c", "chcp 65001 >nul && " + command);
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)))
        {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = process.waitFor();
        if (!output.isEmpty())
        {
            output += "\n";
        }
        return output + "EXIT=" + exitCode;
    }

    private void resetPersistentShell()
    {
        closeQuietly(persistentStdin);
        closeQuietly(persistentStdout);
        if (persistentProcess != null)
        {
            persistentProcess.destroyForcibly();
        }
        persistentProcess = null;
        persistentStdin = null;
        persistentStdout = null;
    }

    private static boolean booleanValue(Object value)
    {
        if (value instanceof Boolean bool)
        {
            return bool;
        }
        if (value instanceof String text)
        {
            return Boolean.parseBoolean(text);
        }
        return false;
    }

    private static void closeQuietly(AutoCloseable closeable)
    {
        if (closeable == null)
        {
            return;
        }
        try
        {
            closeable.close();
        }
        catch (Exception ignored)
        {
        }
    }
}
