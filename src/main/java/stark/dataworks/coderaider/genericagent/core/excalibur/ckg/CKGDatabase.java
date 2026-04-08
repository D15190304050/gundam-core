package stark.dataworks.coderaider.genericagent.core.excalibur.ckg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code Knowledge Graph DB.
 */
// TODO: Introduce graph data structure.
public class CKGDatabase
{
    private final Map<Path, CKGDatabaseEntry> databases = new HashMap<>();
    private static final int MAX_RESPONSE_LEN = 4000;

    private static class CKGDatabaseEntry
    {
        final Path codebasePath;
        final String snapshotHash;
        final Map<String, List<FunctionEntry>> functions = new HashMap<>();
        final Map<String, List<ClassEntry>> classes = new HashMap<>();
        long lastAccessed;

        CKGDatabaseEntry(Path codebasePath, String snapshotHash)
        {
            this.codebasePath = codebasePath;
            this.snapshotHash = snapshotHash;
            this.lastAccessed = System.currentTimeMillis();
        }
    }

    public String searchFunction(Path codebasePath, String identifier, boolean printBody)
    {
        CKGDatabaseEntry entry = getOrCreateEntry(codebasePath);
        List<String> results = new ArrayList<>();
        String lowerId = identifier.toLowerCase();

        for (Map.Entry<String, List<FunctionEntry>> e : entry.functions.entrySet())
        {
            for (FunctionEntry func : e.getValue())
            {
                if (func.getName().toLowerCase().contains(lowerId))
                {
                    results.add(func.toDisplayString(printBody));
                    results.add("---");
                }
            }
        }

        if (results.isEmpty())
        {
            return "No functions found matching: " + identifier;
        }

        return formatResponse(results, "function", identifier);
    }

    public String searchClass(Path codebasePath, String identifier, boolean printBody)
    {
        CKGDatabaseEntry entry = getOrCreateEntry(codebasePath);
        List<String> results = new ArrayList<>();
        String lowerId = identifier.toLowerCase();

        for (Map.Entry<String, List<ClassEntry>> e : entry.classes.entrySet())
        {
            for (ClassEntry cls : e.getValue())
            {
                if (cls.getName().toLowerCase().contains(lowerId))
                {
                    results.add(cls.toDisplayString(printBody));
                    results.add("---");
                }
            }
        }

        if (results.isEmpty())
        {
            return "No classes found matching: " + identifier;
        }

        return formatResponse(results, "class", identifier);
    }

    public String searchClassMethod(Path codebasePath, String identifier, boolean printBody)
    {
        CKGDatabaseEntry entry = getOrCreateEntry(codebasePath);
        List<String> results = new ArrayList<>();
        String lowerId = identifier.toLowerCase();

        for (Map.Entry<String, List<FunctionEntry>> e : entry.functions.entrySet())
        {
            for (FunctionEntry func : e.getValue())
            {
                if (func.getName().toLowerCase().contains(lowerId))
                {
                    results.add(func.toDisplayString(printBody));
                    results.add("---");
                }
            }
        }

        if (results.isEmpty())
        {
            return "No methods found matching: " + identifier;
        }

        return formatResponse(results, "method", identifier);
    }

    private CKGDatabaseEntry getOrCreateEntry(Path codebasePath)
    {
        String hash = computeHash(codebasePath);
        CKGDatabaseEntry entry = databases.get(codebasePath);

        if (entry == null || !entry.snapshotHash.equals(hash))
        {
            entry = new CKGDatabaseEntry(codebasePath, hash);
            buildDatabase(codebasePath, entry);
            databases.put(codebasePath, entry);
        }

        entry.lastAccessed = System.currentTimeMillis();
        return entry;
    }

    private void buildDatabase(Path codebasePath, CKGDatabaseEntry entry)
    {
        if (!Files.exists(codebasePath) || !Files.isDirectory(codebasePath))
        {
            return;
        }

        try
        {
            Files.walk(codebasePath, 10)
                .filter(Files::isRegularFile)
                .filter(p -> isSourceFile(p))
                .forEach(p -> parseFile(p, entry));
        }
        catch (IOException e)
        {
            // Ignore errors during indexing
        }
    }

    private boolean isSourceFile(Path path)
    {
        String ext = getExtension(path.toString().toLowerCase());
        return ext.equals(".py") || ext.equals(".java") ||
               ext.equals(".js") || ext.equals(".jsx") ||
               ext.equals(".ts") || ext.equals(".tsx") ||
               ext.equals(".c") || ext.equals(".cpp") ||
               ext.equals(".h") || ext.equals(".hpp");
    }

    private void parseFile(Path filePath, CKGDatabaseEntry entry)
    {
        try
        {
            String content = Files.readString(filePath);
            String ext = getExtension(filePath.toString());
            String language = getLanguageFromExtension(ext);

            if (language == null)
            {
                return;
            }

            List<FunctionEntry> functions = CodeParser.parseFunctions(content, filePath, language);
            List<ClassEntry> classes = CodeParser.parseClasses(content, filePath, language);

            entry.functions.put(filePath.toString(), functions);
            entry.classes.put(filePath.toString(), classes);
        }
        catch (IOException e)
        {
            // Ignore read errors
        }
    }

    private String computeHash(Path codebasePath)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            Files.walk(codebasePath, 10)
                .filter(Files::isRegularFile)
                .filter(this::isSourceFile)
                .sorted()
                .forEach(p ->
                {
                    try
                    {
                        long size = Files.size(p);
                        long mtime = Files.getLastModifiedTime(p).toMillis();
                        sb.append(p.toString()).append(size).append(mtime);
                    }
                    catch (IOException ignored)
                    {
                    }
                });
        }
        catch (IOException ignored)
        {
        }
        return Integer.toHexString(sb.toString().hashCode());
    }

    private String formatResponse(List<String> results, String type, String identifier)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(results.size() / 2).append(" ").append(type).append("(s) matching '").append(identifier).append("':\n\n");

        for (String r : results)
        {
            if (sb.length() + r.length() > MAX_RESPONSE_LEN)
            {
                sb.append("\n<response clipped>");
                break;
            }
            sb.append(r).append("\n");
        }

        return sb.toString();
    }

    private String getExtension(String filePath)
    {
        int lastDot = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf('/');
        if (lastDot > lastSep)
        {
            return filePath.substring(lastDot);
        }
        return "";
    }

    // Put this method with all supported languages.
    private String getLanguageFromExtension(String ext)
    {
        return switch (ext.toLowerCase())
        {
            case ".py" -> "python";
            case ".java" -> "java";
            case ".js", ".jsx", ".ts", ".tsx" -> "javascript";
            case ".c", ".cpp", ".h", ".hpp", ".cc", ".cxx" -> "c";
            default -> null;
        };
    }
}