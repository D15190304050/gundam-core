package stark.dataworks.coderaider.output;

import java.util.Map;

public interface OutputSchema {
    String name();

    Map<String, String> requiredFields();
}
