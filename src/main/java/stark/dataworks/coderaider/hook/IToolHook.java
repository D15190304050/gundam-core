package stark.dataworks.coderaider.hook;

import java.util.Map;

public interface IToolHook {
    default void beforeTool(String toolName, Map<String, Object> args) {}

    default void afterTool(String toolName, String result) {}
}
