package stark.dataworks.coderaider.genericagent.core.excalibur.ckg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FunctionEntry
{
    private final String name;
    private final String filePath;
    private final String body;
    private final int startLine;
    private final int endLine;
    private final String parentClass;

    public String toDisplayString(boolean printBody)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Function: ").append(name).append("\n");
        sb.append("File: ").append(filePath).append(":").append(startLine).append("-").append(endLine).append("\n");
        if (parentClass != null && !parentClass.isEmpty())
        {
            sb.append("Class: ").append(parentClass).append("\n");
        }
        if (printBody && body != null && !body.isEmpty())
        {
            sb.append("Body:\n").append(body).append("\n");
        }
        return sb.toString();
    }
}