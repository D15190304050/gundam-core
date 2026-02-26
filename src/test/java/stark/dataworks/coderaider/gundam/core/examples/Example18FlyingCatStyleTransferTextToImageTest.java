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
 * 18) Multi-round text-to-image generation: realistic -> cartoon style.
 */
public class Example18FlyingCatStyleTransferTextToImageTest
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

        System.out.println("Round-1 (before): generating realistic flying cat image.");
        GeneratedAsset realistic;
        try
        {
            realistic = client.generate(new ImageGenerationRequest(
                "Generate an image of a flying cat in the sky, with realistic style.",
                MODEL,
                "",
                Map.of("pollIntervalMillis", 5000, "maxPollCount", 60)));
        }
        catch (IllegalStateException ex)
        {
            Assumptions.assumeTrue(false, "Skipped due to remote model access issue: " + ex.getMessage());
            return;
        }

        Path realisticOutputPath = Path.of("src/test/resources/outputs/images/flying-cat-in-sky-realistic.png");
        Files.createDirectories(realisticOutputPath.getParent());
        downloadToFile(realistic.getUri(), realisticOutputPath);
        System.out.println("Round-1 (after): saved to " + realisticOutputPath.toAbsolutePath());

        System.out.println("Round-2 (before): changing style to cartoon.");
        GeneratedAsset cartoon;
        try
        {
            cartoon = client.generate(new ImageGenerationRequest(
                "Based on the same scene of a flying cat in the sky, change the style to cartoon style.",
                MODEL,
                "",
                Map.of("pollIntervalMillis", 5000, "maxPollCount", 60)));
        }
        catch (IllegalStateException ex)
        {
            Assumptions.assumeTrue(false, "Skipped due to remote model access issue: " + ex.getMessage());
            return;
        }

        Path cartoonOutputPath = Path.of("src/test/resources/outputs/images/flying-cat-in-sky-cartoon.png");
        downloadToFile(cartoon.getUri(), cartoonOutputPath);
        System.out.println("Round-2 (after): saved to " + cartoonOutputPath.toAbsolutePath());

        Assertions.assertTrue(Files.exists(realisticOutputPath));
        Assertions.assertTrue(Files.exists(cartoonOutputPath));
        Assertions.assertTrue(Files.size(realisticOutputPath) > 0);
        Assertions.assertTrue(Files.size(cartoonOutputPath) > 0);
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
