package stark.dataworks.coderaider.gundam.core.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import stark.dataworks.coderaider.gundam.core.agent.Agent;
import stark.dataworks.coderaider.gundam.core.agent.AgentDefinition;
import stark.dataworks.coderaider.gundam.core.agent.AgentRegistry;
import stark.dataworks.coderaider.gundam.core.llmspi.adapter.ModelScopeLlmClient;
import stark.dataworks.coderaider.gundam.core.runner.AgentRunner;
import stark.dataworks.coderaider.gundam.core.runner.RunConfiguration;
import stark.dataworks.coderaider.gundam.core.tool.ToolRegistry;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 17) Text-to-image generation with ModelScope + Qwen-Image model.
 */
public class Example17FlyingDragonTextToImageTest
{
    private static final String MODEL = "Qwen/Qwen-Image-2512";
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*]\\(([^)]+)\\)");
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$", Pattern.DOTALL);

    @Test
    public void run()
            throws IOException, InterruptedException
    {
        Dotenv env = Dotenv.configure().filename(".env.local").ignoreIfMalformed().ignoreIfMissing().load();
        String apiKey = env.get("MODEL_SCOPE_API_KEY", System.getenv("MODEL_SCOPE_API_KEY"));
        if (apiKey == null || apiKey.isBlank())
        {
            System.err.println("Error: ModelScope API key is required.");
            return;
        }

        AgentDefinition definition = new AgentDefinition();
        definition.setId("image-dragon-agent");
        definition.setName("image-dragon-agent");
        definition.setModel(MODEL);
        definition.setSystemPrompt("You are an image generation assistant. Always generate an image and include markdown image output.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .build();

        RunConfiguration cfg = new RunConfiguration(8, "image-dragon-session", 0.8, 2048, "none", "text", Map.of());
        String output = runner.chatClient("image-dragon-agent")
            .prompt()
            .user("Generate an image of a flying dragon in the sky. Return markdown image output that includes the generated image URI.")
            .runConfiguration(cfg)
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();

        System.out.println("Model output:\n" + output);
        String imageReference = extractImageReference(output);
        Assumptions.assumeTrue(imageReference != null && !imageReference.isBlank(), "No image reference found in model output.");

        byte[] imageBytes = loadImageBytes(imageReference);
        String extension = inferExtension(imageReference);
        Path outputPath = Path.of("src/test/resources/outputs/images/flying-dragon-in-sky." + extension);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, imageBytes);

        Assertions.assertTrue(Files.exists(outputPath));
        Assertions.assertTrue(Files.size(outputPath) > 0);
        System.out.println("Saved image to: " + outputPath.toAbsolutePath());
    }

    private static String extractImageReference(String content)
    {
        if (content == null || content.isBlank())
        {
            return null;
        }

        Matcher markdownMatcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        if (markdownMatcher.find())
        {
            return markdownMatcher.group(1).trim();
        }

        if (content.contains("data:image/"))
        {
            int index = content.indexOf("data:image/");
            return content.substring(index).trim();
        }

        return null;
    }

    private static byte[] loadImageBytes(String reference)
            throws IOException, InterruptedException
    {
        Matcher dataMatcher = DATA_URL_PATTERN.matcher(reference);
        if (dataMatcher.find())
        {
            return Base64.getDecoder().decode(dataMatcher.group(2));
        }

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        HttpResponse<byte[]> response = client.send(HttpRequest.newBuilder(URI.create(reference)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            throw new IllegalStateException("Failed to download generated image, status=" + response.statusCode());
        }
        return response.body();
    }

    private static String inferExtension(String reference)
    {
        if (reference == null)
        {
            return "png";
        }

        Matcher dataMatcher = DATA_URL_PATTERN.matcher(reference);
        if (dataMatcher.find())
        {
            String mimeType = dataMatcher.group(1);
            if (mimeType.endsWith("jpeg"))
            {
                return "jpg";
            }
            if (mimeType.endsWith("webp"))
            {
                return "webp";
            }
            return "png";
        }

        String lower = reference.toLowerCase();
        if (lower.contains(".jpg") || lower.contains(".jpeg"))
        {
            return "jpg";
        }
        if (lower.contains(".webp"))
        {
            return "webp";
        }
        return "png";
    }
}
