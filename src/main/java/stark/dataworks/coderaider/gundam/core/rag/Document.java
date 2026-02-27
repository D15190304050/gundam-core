package stark.dataworks.coderaider.gundam.core.rag;

import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class Document
{
    private final String id;
    private final String content;
    private final Map<String, Object> metadata;
    private final List<Double> embedding;

    public Document(String id, String content, Map<String, Object> metadata, List<Double> embedding)
    {
        this.id = id;
        this.content = content;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        this.embedding = embedding == null ? List.of() : List.copyOf(embedding);
    }
}
