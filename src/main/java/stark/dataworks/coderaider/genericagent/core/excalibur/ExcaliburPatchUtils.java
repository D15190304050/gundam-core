package stark.dataworks.coderaider.genericagent.core.excalibur;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Patch helpers ported from trae-agent task-completion logic.
 */
public final class ExcaliburPatchUtils
{
    private ExcaliburPatchUtils()
    {
    }

    public static String getGitDiff(Path projectPath, String baseCommit)
    {
        if (projectPath == null || !Files.isDirectory(projectPath))
        {
            return "";
        }
        List<String> command = new ArrayList<>(List.of("git", "--no-pager", "diff", "--binary"));
        if (baseCommit != null && !baseCommit.isBlank())
        {
            command.add(baseCommit);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(projectPath.toFile());
        builder.redirectErrorStream(true);
        try
        {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            return exitCode == 0 ? output : "";
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String removePatchesToTests(String modelPatch)
    {
        if (modelPatch == null || modelPatch.isBlank())
        {
            return "";
        }
        String[] lines = modelPatch.split("(?<=\\n)", -1);
        StringBuilder filtered = new StringBuilder();
        boolean isTests = false;
        for (String line : lines)
        {
            if (line.startsWith("diff --git a/"))
            {
                String[] parts = line.trim().split("\\s+");
                String targetPath = parts.length == 4 ? parts[3] : "";
                isTests = isTestPath(targetPath);
            }
            if (!isTests)
            {
                filtered.append(line);
            }
        }
        return filtered.toString();
    }

    public static boolean hasRequiredPatch(ExcaliburTaskRequest request)
    {
        if (request == null || !request.isMustPatch())
        {
            return true;
        }
        String patch = removePatchesToTests(getGitDiff(request.getProjectPath(), request.getBaseCommit()));
        if (request.getPatchPath() != null)
        {
            writePatchFile(request.getPatchPath(), patch);
        }
        return !patch.isBlank();
    }

    public static String taskIncompleteMessage()
    {
        return "ERROR! Your Patch is empty. Please provide a patch that fixes the problem.";
    }

    private static boolean isTestPath(String targetPath)
    {
        return targetPath.startsWith("b/")
            && (targetPath.contains("/test/")
            || targetPath.contains("/tests/")
            || targetPath.contains("/testing/")
            || targetPath.contains("test_")
            || targetPath.contains("tox.ini"));
    }

    private static void writePatchFile(Path patchPath, String patch)
    {
        try
        {
            Path parent = patchPath.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }
            Files.writeString(patchPath, patch == null ? "" : patch, StandardCharsets.UTF_8);
        }
        catch (IOException ignored)
        {
        }
    }
}
