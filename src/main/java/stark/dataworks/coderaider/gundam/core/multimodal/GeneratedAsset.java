package stark.dataworks.coderaider.gundam.core.multimodal;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * GeneratedAsset stores multimodal output metadata from model/tool generation.
 */
@Getter
public class GeneratedAsset
{
    private final GeneratedAssetType type;
    private final String uri;
    private final String mimeType;
    private final Map<String, Object> metadata;

    public GeneratedAsset(GeneratedAssetType type, String uri, String mimeType, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.uri = Objects.requireNonNull(uri, "uri");
        this.mimeType = mimeType == null ? "" : mimeType;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }
}
