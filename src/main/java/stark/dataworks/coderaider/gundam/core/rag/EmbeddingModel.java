package stark.dataworks.coderaider.gundam.core.rag;

import java.util.List;

public interface EmbeddingModel
{
    List<Double> embed(String text);
}
