package stark.dataworks.coderaider.gundam.core.examples;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.rag.Document;
import stark.dataworks.coderaider.gundam.core.rag.InMemoryVectorStore;
import stark.dataworks.coderaider.gundam.core.rag.RagService;
import stark.dataworks.coderaider.gundam.core.rag.SimpleHashEmbeddingModel;

/**
 * 14) RAG + vector store baseline with streaming-style console output.
 */
public class Example14RagVectorStoreStreamingTest
{
    @Test
    public void run()
    {
        InMemoryVectorStore vectorStore = new InMemoryVectorStore(new SimpleHashEmbeddingModel(64));
        vectorStore.add(List.of(
            new Document("doc-milvus", "Milvus can store high-dimensional vectors for ANN search.", Map.of("backend", "milvus"), List.of()),
            new Document("doc-postgres", "PostgreSQL + pgvector offers vector search with SQL semantics.", Map.of("backend", "postgresql"), List.of()),
            new Document("doc-redis", "Redis can serve vector search and low-latency caching in one stack.", Map.of("backend", "redis"), List.of())));

        RagService ragService = new RagService(vectorStore);
        String context = ragService.retrieveContext("Need vector store with sql support", 2);

        System.out.println("[stream] retrieved_context_start");
        for (String line : context.split("\\n"))
        {
            System.out.println("[stream] " + line);
        }
        System.out.println("[stream] retrieved_context_end");

        assertTrue(context.contains("PostgreSQL") || context.contains("vector"));
    }
}
