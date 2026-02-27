package stark.dataworks.coderaider.gundam.core.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic local embedding model for tests/examples.
 */
public class SimpleHashEmbeddingModel implements EmbeddingModel
{
    private final int dimensions;

    public SimpleHashEmbeddingModel(int dimensions)
    {
        this.dimensions = Math.max(8, dimensions);
    }

    @Override
    public List<Double> embed(String text)
    {
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < dimensions; i++)
        {
            vector.add(0d);
        }
        if (text == null)
        {
            return vector;
        }
        for (int i = 0; i < text.length(); i++)
        {
            int bucket = i % dimensions;
            vector.set(bucket, vector.get(bucket) + (double) text.charAt(i));
        }
        return vector;
    }
}
