package stark.dataworks.coderaider.gundam.core.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default vector store implementation; swap with milvus/postgresql/redis-backed implementations via interface.
 */
public class InMemoryVectorStore implements VectorStore
{
    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final EmbeddingModel embeddingModel;

    public InMemoryVectorStore(EmbeddingModel embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void add(List<Document> documents)
    {
        if (documents == null)
        {
            return;
        }
        for (Document document : documents)
        {
            List<Double> embedding = document.getEmbedding().isEmpty()
                ? embeddingModel.embed(document.getContent())
                : document.getEmbedding();
            this.documents.put(document.getId(), new Document(document.getId(), document.getContent(), document.getMetadata(), embedding));
        }
    }

    @Override
    public void delete(List<String> ids)
    {
        if (ids == null)
        {
            return;
        }
        ids.forEach(documents::remove);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request)
    {
        List<Double> queryEmbedding = embeddingModel.embed(request.getQuery());
        List<Map.Entry<Document, Double>> scored = new ArrayList<>();
        for (Document document : documents.values())
        {
            if (!matchesFilter(document, request.getFilter()))
            {
                continue;
            }
            double score = cosine(queryEmbedding, document.getEmbedding());
            if (request.getSimilarityThreshold() > -1 && score < request.getSimilarityThreshold())
            {
                continue;
            }
            scored.add(Map.entry(document, score));
        }
        scored.sort(Comparator.comparingDouble(Map.Entry<Document, Double>::getValue).reversed());
        List<Document> result = new ArrayList<>();
        int max = Math.min(request.getTopK(), scored.size());
        for (int i = 0; i < max; i++)
        {
            Map<String, Object> meta = new HashMap<>(scored.get(i).getKey().getMetadata());
            meta.put("score", scored.get(i).getValue());
            Document base = scored.get(i).getKey();
            result.add(new Document(base.getId(), base.getContent(), meta, base.getEmbedding()));
        }
        return result;
    }

    private boolean matchesFilter(Document document, Map<String, Object> filter)
    {
        for (Map.Entry<String, Object> entry : filter.entrySet())
        {
            if (!entry.getValue().equals(document.getMetadata().get(entry.getKey())))
            {
                return false;
            }
        }
        return true;
    }

    private double cosine(List<Double> left, List<Double> right)
    {
        int dimensions = Math.min(left.size(), right.size());
        if (dimensions == 0)
        {
            return 0;
        }
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < dimensions; i++)
        {
            dot += left.get(i) * right.get(i);
            leftNorm += left.get(i) * left.get(i);
            rightNorm += right.get(i) * right.get(i);
        }
        if (leftNorm == 0 || rightNorm == 0)
        {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
