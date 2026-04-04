package stark.dataworks.coderaider.genericagent.core.excalibur.ckg;

import org.antlr.v4.runtime.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// TODO: add support for more languages.
// To be implemented (for current roadmap): Java, Python, JS, TS, C#, Go.
public class CodeParser
{
    public static List<FunctionEntry> parseFunctions(String content, Path filePath, String language)
    {
        return switch (language)
        {
            case "java" -> parseJavaFunctions(content, filePath);
            case "python" -> parsePythonFunctions(content, filePath);
            case "javascript" -> parseJavaScriptFunctions(content, filePath);
            default -> new ArrayList<>();
        };
    }

    public static List<ClassEntry> parseClasses(String content, Path filePath, String language)
    {
        return switch (language)
        {
            case "java" -> parseJavaClasses(content, filePath);
            case "python" -> parsePythonClasses(content, filePath);
            case "javascript" -> parseJavaScriptClasses(content, filePath);
            default -> new ArrayList<>();
        };
    }

    public static List<FunctionEntry> searchFunctions(String content, Path filePath, String identifier)
    {
        String ext = getExtension(filePath.toString());
        String language = getLanguageFromExtension(ext);
        if (language == null)
        {
            return new ArrayList<>();
        }

        List<FunctionEntry> allFunctions = parseFunctions(content, filePath, language);
        List<FunctionEntry> results = new ArrayList<>();
        String lowerId = identifier.toLowerCase();

        for (FunctionEntry func : allFunctions)
        {
            if (func.getName().toLowerCase().contains(lowerId))
            {
                results.add(func);
            }
        }
        return results;
    }

    public static List<ClassEntry> searchClasses(String content, Path filePath, String identifier)
    {
        String ext = getExtension(filePath.toString());
        String language = getLanguageFromExtension(ext);
        if (language == null)
        {
            return new ArrayList<>();
        }

        List<ClassEntry> allClasses = parseClasses(content, filePath, language);
        List<ClassEntry> results = new ArrayList<>();
        String lowerId = identifier.toLowerCase();

        for (ClassEntry cls : allClasses)
        {
            if (cls.getName().toLowerCase().contains(lowerId))
            {
                results.add(cls);
            }
        }
        return results;
    }

    public static List<FunctionEntry> searchClassMethods(String content, Path filePath, String className)
    {
        return searchFunctions(content, filePath, className);
    }

    private static List<FunctionEntry> parseJavaFunctions(String content, Path filePath)
    {
        List<FunctionEntry> functions = new ArrayList<>();
        String[] lines = content.split("\n");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:(?:public|private|protected)?\\s*)?(?:static\\s*)?(?:final\\s*)?(?:\\w+(?:<[^>]+>)?(?:\\s+|\\*)+)(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+[^{]+)?\\s*\\{",
            java.util.regex.Pattern.MULTILINE);

        for (int i = 0; i < lines.length; i++)
        {
            java.util.regex.Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find())
            {
                String name = matcher.group(1);
                if (isValidMethodName(name) && !isTypeDeclaration(name))
                {
                    int endLine = findMatchingBraceLine(lines, i);
                    String body = extractBody(lines, i, endLine);
                    functions.add(new FunctionEntry(name, filePath.toString(), body, i + 1, endLine, null));
                }
            }
        }
        return functions;
    }

    private static List<FunctionEntry> parsePythonFunctions(String content, Path filePath)
    {
        List<FunctionEntry> functions = new ArrayList<>();
        String[] lines = content.split("\n");
        int indentStack = 0;
        boolean inFunction = false;
        int funcStartLine = 0;
        String funcName = null;
        int funcIndent = 0;

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

            java.util.regex.Pattern defPattern = java.util.regex.Pattern.compile("^\\s*(?:async\\s+)?def\\s+(\\w+)\\s*\\(");
            java.util.regex.Matcher defMatcher = defPattern.matcher(line);
            if (defMatcher.find())
            {
                inFunction = true;
                funcName = defMatcher.group(1);
                funcStartLine = i + 1;
                funcIndent = line.indexOf('d');
                indentStack = 0;
                continue;
            }

            if (inFunction)
            {
                if (line.trim().isEmpty())
                {
                    indentStack++;
                    continue;
                }

                int currentIndent = getIndent(line);
                if (currentIndent <= funcIndent && !line.trim().startsWith("#"))
                {
                    int endLine = i;
                    String body = extractPythonBody(lines, funcStartLine - 1, endLine);
                    functions.add(new FunctionEntry(funcName, filePath.toString(), body, funcStartLine, endLine, null));
                    inFunction = false;
                    funcName = null;
                }
            }
        }

        if (inFunction)
        {
            int endLine = lines.length;
            String body = extractPythonBody(lines, funcStartLine - 1, endLine);
            functions.add(new FunctionEntry(funcName, filePath.toString(), body, funcStartLine, endLine, null));
        }

        return functions;
    }

    private static List<FunctionEntry> parseJavaScriptFunctions(String content, Path filePath)
    {
        List<FunctionEntry> functions = new ArrayList<>();
        String[] lines = content.split("\n");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:async\\s+)?function\\s+(\\w+)\\s*\\(",
            java.util.regex.Pattern.MULTILINE);

        for (int i = 0; i < lines.length; i++)
        {
            java.util.regex.Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find())
            {
                String name = matcher.group(1);
                int endLine = findMatchingBraceLine(lines, i);
                String body = extractBody(lines, i, endLine);
                functions.add(new FunctionEntry(name, filePath.toString(), body, i + 1, endLine, null));
            }
        }
        return functions;
    }

    private static List<ClassEntry> parseJavaClasses(String content, Path filePath)
    {
        List<ClassEntry> classes = new ArrayList<>();
        String[] lines = content.split("\n");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)",
            java.util.regex.Pattern.MULTILINE);

        for (int i = 0; i < lines.length; i++)
        {
            java.util.regex.Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find())
            {
                String name = matcher.group(1);
                int endLine = findMatchingBraceLine(lines, i);
                String body = extractBody(lines, i, endLine);

                List<String> methods = extractJavaMethods(body);
                List<String> fields = extractJavaFields(body);

                classes.add(new ClassEntry(
                    name,
                    filePath.toString(),
                    body,
                    i + 1,
                    endLine,
                    methods.isEmpty() ? null : String.join(", ", methods),
                    fields.isEmpty() ? null : String.join(", ", fields)));
            }
        }
        return classes;
    }

    private static List<ClassEntry> parsePythonClasses(String content, Path filePath)
    {
        List<ClassEntry> classes = new ArrayList<>();
        String[] lines = content.split("\n");
        boolean inClass = false;
        int classStartLine = 0;
        String className = null;
        int classIndent = 0;
        List<String> methods = new ArrayList<>();

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

            java.util.regex.Pattern classPattern = java.util.regex.Pattern.compile("^\\s*class\\s+(\\w+)");
            java.util.regex.Matcher classMatcher = classPattern.matcher(line);
            if (classMatcher.find())
            {
                if (inClass)
                {
                    String body = extractPythonBody(lines, classStartLine - 1, i);
                    classes.add(new ClassEntry(className, filePath.toString(), body, classStartLine, i, methods.isEmpty() ? null : String.join(", ", methods), null));
                    methods.clear();
                }
                inClass = true;
                className = classMatcher.group(1);
                classStartLine = i + 1;
                classIndent = line.indexOf('c');
                continue;
            }

            if (inClass)
            {
                java.util.regex.Pattern methodPattern = java.util.regex.Pattern.compile("^\\s+def\\s+(\\w+)\\s*\\(");
                java.util.regex.Matcher methodMatcher = methodPattern.matcher(line);
                if (methodMatcher.find())
                {
                    methods.add(methodMatcher.group(1) + "()");
                }

                int currentIndent = getIndent(line);
                if (currentIndent <= classIndent && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                {
                    String body = extractPythonBody(lines, classStartLine - 1, i);
                    classes.add(new ClassEntry(className, filePath.toString(), body, classStartLine, i, methods.isEmpty() ? null : String.join(", ", methods), null));
                    methods.clear();
                    inClass = false;
                    i--;
                }
            }
        }

        if (inClass)
        {
            String body = extractPythonBody(lines, classStartLine - 1, lines.length);
            classes.add(new ClassEntry(className, filePath.toString(), body, classStartLine, lines.length, methods.isEmpty() ? null : String.join(", ", methods), null));
        }

        return classes;
    }

    private static List<ClassEntry> parseJavaScriptClasses(String content, Path filePath)
    {
        List<ClassEntry> classes = new ArrayList<>();
        String[] lines = content.split("\n");

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "class\\s+(\\w+)(?:\\s+extends\\s+\\w+)?\\s*\\{",
            java.util.regex.Pattern.MULTILINE);

        for (int i = 0; i < lines.length; i++)
        {
            java.util.regex.Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find())
            {
                String name = matcher.group(1);
                int endLine = findMatchingBraceLine(lines, i);
                String body = extractBody(lines, i, endLine);

                List<String> methods = extractJsMethods(body);
                List<String> fields = extractJsFields(body);

                classes.add(new ClassEntry(
                    name,
                    filePath.toString(),
                    body,
                    i + 1,
                    endLine,
                    methods.isEmpty() ? null : String.join(", ", methods),
                    fields.isEmpty() ? null : String.join(", ", fields)));
            }
        }
        return classes;
    }

    private static List<String> extractJavaMethods(String body)
    {
        List<String> methods = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static)?\\s*(?:final)?\\s*\\w+(?:<[^>]+>)?(?:\\s+|\\*)+(\\w+)\\s*\\(");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        while (matcher.find())
        {
            String name = matcher.group(1);
            if (isValidMethodName(name))
            {
                methods.add(name + "()");
            }
        }
        return methods;
    }

    private static List<String> extractJavaFields(String body)
    {
        List<String> fields = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:private|public|protected)?\\s*(?:static)?\\s*(?:final)?\\s*\\w+(?:<[^>]+>)?(?:\\s+|\\*)+(\\w+)\\s*=");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        while (matcher.find())
        {
            fields.add(matcher.group(1));
        }
        return fields;
    }

    private static List<String> extractJsMethods(String body)
    {
        List<String> methods = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:static\\s+)?(?:async\\s+)?(\\w+)\\s*\\([^)]*\\)\\s*\\{");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        while (matcher.find())
        {
            String name = matcher.group(1);
            if (!name.equals("if") && !name.equals("for") && !name.equals("while") && !name.equals("switch"))
            {
                methods.add(name + "()");
            }
        }
        return methods;
    }

    private static List<String> extractJsFields(String body)
    {
        List<String> fields = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:static\\s+)?(\\w+)\\s*=\\s*[^;]+;");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        while (matcher.find())
        {
            fields.add(matcher.group(1));
        }
        return fields;
    }

    private static boolean isValidMethodName(String name)
    {
        return !name.equals("if") && !name.equals("for") && !name.equals("while") &&
               !name.equals("switch") && !name.equals("class") && !name.equals("interface") &&
               !name.equals("enum") && !name.equals("return") && !name.equals("import");
    }

    private static boolean isTypeDeclaration(String name)
    {
        return name.startsWith("Map") || name.startsWith("List") || name.startsWith("Set") ||
               name.startsWith("String") || name.startsWith("Integer") || name.startsWith("Boolean") ||
               name.equals("void") || name.equals("int") || name.equals("long") ||
               name.equals("double") || name.equals("float") || name.equals("boolean") ||
               name.equals("char") || name.equals("byte") || name.equals("short");
    }

    private static int findMatchingBraceLine(String[] lines, int startLine)
    {
        int braceCount = 0;
        for (int i = startLine; i < lines.length; i++)
        {
            for (char c : lines[i].toCharArray())
            {
                if (c == '{')
                {
                    braceCount++;
                }
                else if (c == '}')
                {
                    braceCount--;
                    if (braceCount == 0)
                    {
                        return i + 1;
                    }
                }
            }
        }
        return lines.length;
    }

    private static String extractBody(String[] lines, int startLine, int endLine)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < endLine && i < lines.length; i++)
        {
            sb.append(lines[i]).append("\n");
        }
        String body = sb.toString();
        if (body.length() > 500)
        {
            body = body.substring(0, 500) + "\n... [truncated]";
        }
        return body;
    }

    private static String extractPythonBody(String[] lines, int startLine, int endLine)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < endLine && i < lines.length; i++)
        {
            sb.append(lines[i]).append("\n");
        }
        String body = sb.toString();
        if (body.length() > 500)
        {
            body = body.substring(0, 500) + "\n... [truncated]";
        }
        return body;
    }

    private static int getIndent(String line)
    {
        int indent = 0;
        for (char c : line.toCharArray())
        {
            if (c == ' ' || c == '\t')
            {
                indent++;
            }
            else
            {
                break;
            }
        }
        return indent;
    }

    private static String getExtension(String filePath)
    {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0)
        {
            return filePath.substring(lastDot);
        }
        return "";
    }

    private static String getLanguageFromExtension(String ext)
    {
        return switch (ext.toLowerCase())
        {
            case ".py" -> "python";
            case ".java" -> "java";
            case ".js", ".jsx", ".ts", ".tsx" -> "javascript";
            case ".c", ".cpp", ".h", ".hpp" -> "c";
            default -> null;
        };
    }
}