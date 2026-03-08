package stark.dataworks.coderaider.gundam.core.examples;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

final class McpTestSupport
{
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean WINDOWS = OS_NAME.contains("win");
    private static final boolean MACOS = OS_NAME.contains("mac");
    private static final boolean LINUX = OS_NAME.contains("linux");

    private McpTestSupport()
    {
    }

    static String pythonExecutable()
    {
        if (WINDOWS)
        {
            if (commandExists("python"))
            {
                return "python";
            }
            if (commandExists("py"))
            {
                return "py";
            }
            throw new IllegalStateException("Neither python nor py is available in PATH");
        }

        if (commandExists("python3"))
        {
            return "python3";
        }
        if (commandExists("python"))
        {
            return "python";
        }
        throw new IllegalStateException("Neither python3 nor python is available in PATH");
    }

    static Process startPythonScript(String scriptPath, String... args)
    {
        String[] command = new String[2 + args.length];
        command[0] = pythonExecutable();
        command[1] = scriptPath;
        System.arraycopy(args, 0, command, 2, args.length);
        try
        {
            return new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Failed to start MCP script: " + scriptPath, ex);
        }
    }

    static void waitForHttp(String url, Duration timeout)
    {
        long deadline = System.nanoTime() + timeout.toNanos();
        RuntimeException last = null;
        while (System.nanoTime() < deadline)
        {
            try
            {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(500);
                connection.setReadTimeout(500);
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                if (code >= 200 && code < 500)
                {
                    return;
                }
            }
            catch (Exception ex)
            {
                last = new RuntimeException(ex);
            }

            try
            {
                Thread.sleep(150);
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for MCP server", ex);
            }
        }
        throw new IllegalStateException("MCP HTTP endpoint did not become ready in time: " + url, last);
    }

    static void stopQuietly(Process process)
    {
        if (process == null)
        {
            return;
        }
        process.destroy();
    }

    private static boolean commandExists(String command)
    {
        try
        {
            if (WINDOWS)
            {
                if (runCommand("cmd", "/c", "where", command))
                {
                    return true;
                }
            }
            else if (MACOS || LINUX)
            {
                if (runCommand("sh", "-lc", "command -v " + command))
                {
                    return true;
                }
                if (runCommand("which", command))
                {
                    return true;
                }
            }
            else
            {
                if (runCommand("sh", "-lc", "command -v " + command))
                {
                    return true;
                }
            }
            return existsInPath(command);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private static boolean runCommand(String... command)
    {
        try
        {
            Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
            return process.waitFor() == 0;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private static boolean existsInPath(String command)
    {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank())
        {
            return false;
        }

        String[] entries = path.split(java.io.File.pathSeparator);
        if (WINDOWS)
        {
            String pathExt = System.getenv("PATHEXT");
            String[] extensions = (pathExt == null || pathExt.isBlank())
                ? new String[]{".EXE", ".BAT", ".CMD"}
                : pathExt.split(";");
            for (String entry : entries)
            {
                for (String extension : extensions)
                {
                    java.io.File file = new java.io.File(entry, command + extension.toLowerCase());
                    if (file.isFile())
                    {
                        return true;
                    }
                    file = new java.io.File(entry, command + extension.toUpperCase());
                    if (file.isFile())
                    {
                        return true;
                    }
                }
                java.io.File direct = new java.io.File(entry, command);
                if (direct.isFile())
                {
                    return true;
                }
            }
            return false;
        }

        for (String entry : entries)
        {
            java.io.File file = new java.io.File(entry, command);
            if (file.isFile() && file.canExecute())
            {
                return true;
            }
        }
        return false;
    }

}
