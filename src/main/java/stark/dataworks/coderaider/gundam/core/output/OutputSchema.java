package stark.dataworks.coderaider.gundam.core.output;

import java.util.Map;

public interface OutputSchema {
    String name();

    Map<String, String> requiredFields();
}
