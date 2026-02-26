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
        GeneratedImageSaveHook saveHook = new GeneratedImageSaveHook();

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

        Path outputPath = saveHook.onGenerated("Dragon generation", generated, "flying-dragon-in-sky.png");
        Assertions.assertTrue(Files.exists(outputPath));
        Assertions.assertTrue(Files.size(outputPath) > 0);
        System.out.println("Generated URI: " + generated.getUri());
    }
}
