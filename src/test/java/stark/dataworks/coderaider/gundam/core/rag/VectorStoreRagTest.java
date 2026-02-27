package stark.dataworks.coderaider.gundam.core.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class VectorStoreRagTest
{
    @Test
    void supportsSimilaritySearchAndFiltering()
    {
        InMemoryVectorStore vectorStore = new InMemoryVectorStore(new SimpleHashEmbeddingModel(32));
        vectorStore.add(List.of(
            new Document("d1", "Redis is often used for caching", Map.of("store", "redis"), List.of()),
            new Document("d2", "Milvus is a dedicated vector database", Map.of("store", "milvus"), List.of()),
            new Document("d3", "Postgresql with pgvector supports embeddings", Map.of("store", "postgres"), List.of())));

        List<Document> redisOnly = vectorStore.similaritySearch(new SearchRequest("cache redis", 2, -1, Map.of("store", "redis")));
        assertEquals(1, redisOnly.size());
        assertEquals("d1", redisOnly.getFirst().getId());

        RagService ragService = new RagService(vectorStore);
        String context = ragService.retrieveContext("vector database", 2);
        assertFalse(context.isBlank());
    }
}
