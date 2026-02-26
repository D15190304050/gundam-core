package stark.dataworks.coderaider.gundam.core.examples;

import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Hook-style helper for downloading and storing generated images.
 */
public class GeneratedImageSaveHook
{
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    public Path onGenerated(String stage, GeneratedAsset generatedAsset, String fileName)
            throws IOException, InterruptedException
    {
        Path outputPath = Path.of("src/test/resources/outputs/images/" + fileName);
        Files.createDirectories(outputPath.getParent());

        HttpResponse<byte[]> response = httpClient.send(
            HttpRequest.newBuilder(URI.create(generatedAsset.getUri())).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            throw new IllegalStateException("Failed to download generated image, status=" + response.statusCode());
        }

        Files.write(outputPath, response.body());
        System.out.println(stage + " (after): saved to " + outputPath.toAbsolutePath());
        return outputPath;
    }
}
