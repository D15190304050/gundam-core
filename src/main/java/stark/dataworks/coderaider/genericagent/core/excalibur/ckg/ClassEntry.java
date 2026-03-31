package stark.dataworks.coderaider.genericagent.core.excalibur.ckg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassEntry
{
    private final String name;
    private final String filePath;
    private final String body;
    private final int startLine;
    private final int endLine;
    private final String methods;
    private final String fields;

    public String toDisplayString(boolean printBody)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(name).append("\n");
        sb.append("File: ").append(filePath).append(":").append(startLine).append("-").append(endLine).append("\n");
        if (fields != null && !fields.isEmpty())
        {
            sb.append("Fields: ").append(fields).append("\n");
        }
        if (methods != null && !methods.isEmpty())
        {
            sb.append("Methods: ").append(methods).append("\n");
        }
        if (printBody && body != null && !body.isEmpty())
        {
            sb.append("Body:\n").append(body).append("\n");
        }
        return sb.toString();
    }
}