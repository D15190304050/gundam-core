package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.ImageGenerationRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        GeneratedImageSaveHook saveHook = new GeneratedImageSaveHook();

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

        Path realisticOutputPath = saveHook.onGenerated("Round-1", realistic, "flying-cat-in-sky-realistic.png");

        System.out.println("Round-2 (before): changing style to cartoon with the round-1 image as source.");
        GeneratedAsset cartoon;
        try
        {
            cartoon = client.generate(new ImageGenerationRequest(
                "Use the provided source image and regenerate the same scene in cartoon style.",
                MODEL,
                "",
                Map.of(
                    "image_url", realistic.getUri(),
                    "pollIntervalMillis", 5000,
                    "maxPollCount", 60)));
        }
        catch (IllegalStateException ex)
        {
            Assumptions.assumeTrue(false, "Skipped due to remote model access issue: " + ex.getMessage());
            return;
        }

        Path cartoonOutputPath = saveHook.onGenerated("Round-2", cartoon, "flying-cat-in-sky-cartoon.png");

        Assertions.assertTrue(Files.exists(realisticOutputPath));
        Assertions.assertTrue(Files.exists(cartoonOutputPath));
        Assertions.assertTrue(Files.size(realisticOutputPath) > 0);
        Assertions.assertTrue(Files.size(cartoonOutputPath) > 0);
    }
}
