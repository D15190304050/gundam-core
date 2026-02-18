package stark.dataworks.coderaider.tool;

import java.util.Optional;

public interface IToolRegistry {
    void register(ITool tool);

    Optional<ITool> get(String toolName);
}
