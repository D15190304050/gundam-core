package stark.dataworks.coderaider.genericagent.core.excalibur.tools;

import stark.dataworks.coderaider.genericagent.core.editor.IApplyPatchEditor;
import stark.dataworks.coderaider.genericagent.core.tool.ToolRegistry;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.ApplyPatchTool;
import stark.dataworks.coderaider.genericagent.core.tool.builtin.LocalShellTool;
import stark.dataworks.coderaider.genericagent.core.tool.ToolDefinition;
import stark.dataworks.coderaider.genericagent.core.tool.ToolParameterSchema;

import java.nio.file.Path;
import java.util.List;

/**
 * Registers Trae-compatible Excalibur tools on a tool registry.
 */
public final class ExcaliburToolRegistrySupport
{
    private ExcaliburToolRegistrySupport()
    {
    }

    public static void registerTraeCompatibleTools(ToolRegistry toolRegistry, Path workspace, IApplyPatchEditor editor)
    {
        toolRegistry.register(new ExcaliburBashTool(workspace));
        toolRegistry.register(new ExcaliburSequentialThinkingTool());
        toolRegistry.register(new ExcaliburTaskDoneTool());
        toolRegistry.register(new ExcaliburStrReplaceBasedEditTool(workspace));
        toolRegistry.register(new ExcaliburJsonEditTool(workspace));
        toolRegistry.register(new LocalShellTool(new ToolDefinition(
            "local_shell",
            "Execute a local shell command and return stdout/stderr.",
            List.of(new ToolParameterSchema("command", "string", true, "Shell command to execute")))));
        toolRegistry.register(new ApplyPatchTool(editor, false));
    }
}
