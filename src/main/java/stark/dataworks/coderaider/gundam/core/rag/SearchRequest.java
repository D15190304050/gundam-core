package stark.dataworks.coderaider.gundam.core.rag;

import java.util.Map;

import lombok.Getter;

@Getter
public class SearchRequest
{
    private final String query;
    private final int topK;
    private final double similarityThreshold;
    private final Map<String, Object> filter;

    public SearchRequest(String query, int topK, double similarityThreshold, Map<String, Object> filter)
    {
        this.query = query;
        this.topK = topK < 1 ? 4 : topK;
        this.similarityThreshold = similarityThreshold;
        this.filter = filter == null ? Map.of() : Map.copyOf(filter);
    }

    public static SearchRequest defaults(String query)
    {
        return new SearchRequest(query, 4, -1, Map.of());
    }
}
