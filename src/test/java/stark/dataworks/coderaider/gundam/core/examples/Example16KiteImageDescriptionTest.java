package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.multimodal.MessagePart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class Example16KiteImageDescriptionTest
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

        String imageDataUrl = asDataUrl(Path.of("src/main/resources/images/inputs/kite.webp"), "image/webp");
        LlmRequest request = new LlmRequest(
            model,
            List.of(new Message(Role.USER, List.of(
                MessagePart.text("Describe this image in detail."),
                MessagePart.image(imageDataUrl, "image/webp")))),
            List.of(),
            new LlmOptions(0.4, 1024));

        ModelScopeLlmClient client = new ModelScopeLlmClient(apiKey, model);
        try
        {
            LlmResponse response = client.chat(request);
            System.out.println("Kite image description:\n" + response.getContent());
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
