package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.ImageGenerationRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * 17) Text-to-image generation with ModelScope + Qwen-Image model.
 */
public class Example17FlyingDragonTextToImageTest
{
    private static final String MODEL = "Qwen/Qwen-Image-2512";

    @Test
    public void run()
            throws IOException, InterruptedException
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        Assertions.assertNotNull(apiKey, "MODEL_SCOPE_API_KEY is required");

        ModelScopeLlmClient client = new ModelScopeLlmClient(apiKey, MODEL);

        System.out.println("Before generation: I will generate an image of a flying dragon in the sky.");
        GeneratedAsset generated;
        try
        {
            generated = client.generate(new ImageGenerationRequest(
                "Generate an image of a flying dragon in the sky.",
                MODEL,
                "",
                Map.of("pollIntervalMillis", 5000, "maxPollCount", 60)));
        }
        catch (IllegalStateException ex)
        {
            Assumptions.assumeTrue(false, "Skipped due to remote model access issue: " + ex.getMessage());
            return;
        }

        Path outputPath = Path.of("src/test/resources/outputs/images/flying-dragon-in-sky.png");
        Files.createDirectories(outputPath.getParent());
        downloadToFile(generated.getUri(), outputPath);

        Assertions.assertTrue(Files.exists(outputPath));
        Assertions.assertTrue(Files.size(outputPath) > 0);
        System.out.println("After generation: saved generated dragon image to " + outputPath.toAbsolutePath());
        System.out.println("Generated URI: " + generated.getUri());
    }

    private static void downloadToFile(String url, Path outputPath)
            throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        HttpResponse<byte[]> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            throw new IllegalStateException("Failed to download generated image, status=" + response.statusCode());
        }
        Files.write(outputPath, response.body());
    }
}
