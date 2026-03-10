package stark.dataworks.coderaider.genericagent.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.genericagent.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.genericagent.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.genericagent.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.genericagent.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.genericagent.core.context.ContextItem;
import stark.dataworks.coderaider.genericagent.core.model.Role;
import stark.dataworks.coderaider.genericagent.core.multimodal.MessagePart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class Example15IntegralImageQuestionTest
{
    @Test
    public void run()
        throws IOException
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();

        String model = "Qwen/Qwen3.5-27B";
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            return;
        }

        String imageDataUrl = asDataUrl(Path.of("src/main/resources/images/inputs/IntegralQuestion01.png"), "image/png");
        LlmRequest request = new LlmRequest(
            model,
            List.of(new ContextItem(Role.USER, List.of(
                MessagePart.text("Please solve this integral problem from the image. Show key steps and the final answer."),
                MessagePart.image(imageDataUrl, "image/png")))),
            List.of(),
            new LlmOptions(0.2, 4096));

        ModelScopeLlmClient client = new ModelScopeLlmClient(apiKey, model);
        try
        {
            LlmResponse response = client.chat(request);
            System.out.println("Integral solution:\n" + response.getContent());
        }
        catch (IllegalStateException ex)
        {
            Assumptions.assumeTrue(false, "Skipped due to remote model access issue: " + ex.getMessage());
        }
    }

    private static String asDataUrl(Path imagePath, String mimeType)
        throws IOException
    {
        byte[] bytes = Files.readAllBytes(imagePath);
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
