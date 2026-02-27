package stark.dataworks.coderaider.gundam.core.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.context.ContextResult;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.rag.Document;
import stark.dataworks.coderaider.gundam.core.rag.InMemoryVectorStore;
import stark.dataworks.coderaider.gundam.core.rag.RagService;
import stark.dataworks.coderaider.gundam.core.rag.SimpleHashEmbeddingModel;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

/**
 * 14) RAG + vector store baseline with streaming-style console output.
 */
public class Example20RagVectorStoreStreamingTest
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

    @Test
    public void multiRoundConversationRetrievesContextBeforeLlmResponse()
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String model = "Qwen/Qwen3-4B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        assertNotNull(apiKey, "MODEL_SCOPE_API_KEY is required");
        assertFalse(apiKey.isBlank(), "MODEL_SCOPE_API_KEY is required");

        InMemoryVectorStore vectorStore = new InMemoryVectorStore(new SimpleHashEmbeddingModel(64));
        vectorStore.add(List.of(
            new Document("doc-attention", "In transformers, attention scores decide which tokens receive focus when answering a query.", Map.of("topic", "attention"), List.of()),
            new Document("doc-self-attention", "Self-attention computes interactions between tokens in the same sequence to build contextual meaning.", Map.of("topic", "attention"), List.of()),
            new Document("doc-index", "A vector index stores embeddings so retrieval can happen before the generator drafts a final response.", Map.of("topic", "rag"), List.of())));

        RagService ragService = new RagService(vectorStore);

        AgentDefinition agentDef = new AgentDefinition();
        agentDef.setId("rag-streaming-agent");
        agentDef.setName("RAG Streaming Agent");
        agentDef.setModel(model);
        agentDef.setSystemPrompt("You are a concise assistant. Use the provided context section to answer each question.");

        AgentRegistry agentRegistry = new AgentRegistry();
        Agent agent = new Agent(agentDef);
        agentRegistry.register(agent);

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, model))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(agentRegistry)
            .eventPublisher(ExampleStreamingPublishers.textOnly())
            .build();

        List<String> userTurns = List.of(
            "Round 1: What does attention do in transformers?",
            "Round 2: How is self-attention different from generic attention?",
            "Round 3: In a RAG flow, when should retrieval happen relative to LLM generation?");

        RunConfiguration cfg = new RunConfiguration(8, "example20-rag-session", 0.2, 512, "auto", "text", Map.of());

        for (int i = 0; i < userTurns.size(); i++)
        {
            String userInput = userTurns.get(i);
            List<String> roundTimeline = new ArrayList<>();
            roundTimeline.add("rag_retrieve_start");
            String retrievedContext = ragService.retrieveContext(userInput, 2);
            roundTimeline.add("rag_retrieve_end");

            assertFalse(retrievedContext.isBlank(), "round " + (i + 1) + " should retrieve RAG context");

            String prompt = "User question: " + userInput + "\n"
                + "Retrieved context (must read before answering):\n" + retrievedContext;

            roundTimeline.add("llm_generate_start");
            ContextResult roundResult = runner.runStreamed(agent, prompt, cfg, ExampleSupport.noopHooks());
            roundTimeline.add("llm_generate_end");

            assertFalse(roundResult.getFinalOutput().isBlank());
            assertTrue(roundTimeline.indexOf("rag_retrieve_end") < roundTimeline.indexOf("llm_generate_start"),
                "round " + (i + 1) + " should complete retrieval before generation");
        }

        assertEquals(3, userTurns.size());
    }
}
