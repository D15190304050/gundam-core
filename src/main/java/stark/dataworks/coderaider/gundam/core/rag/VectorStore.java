package stark.dataworks.coderaider.gundam.core.rag;

import java.util.List;

/**
 * Spring-AI-like vector store contract.
 */
public interface VectorStore
{
    void add(List<Document> documents);

    void delete(List<String> ids);

    List<Document> similaritySearch(SearchRequest request);
}
