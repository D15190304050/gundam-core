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
 * 18) Multi-round text-to-image generation: realistic -> cartoon style.
 */
public class Example18FlyingCatStyleTransferTextToImageTest
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
        definition.setId("image-cat-agent");
        definition.setName("image-cat-agent");
        definition.setModel(MODEL);
        definition.setSystemPrompt("You are an image generation assistant. Always generate an image and include markdown image output.");

        AgentRegistry registry = new AgentRegistry();
        registry.register(new Agent(definition));

        AgentRunner runner = AgentRunner.builder()
            .llmClient(new ModelScopeLlmClient(apiKey, MODEL))
            .toolRegistry(new ToolRegistry())
            .agentRegistry(registry)
            .build();

        RunConfiguration cfg = new RunConfiguration(12, "image-cat-style-session", 0.8, 2048, "none", "text", Map.of());

        String firstOutput = runner.chatClient("image-cat-agent")
            .prompt()
            .user("Generate an image of a flying cat in the sky, with realistic style. Return markdown image output that includes the generated image URI.")
            .runConfiguration(cfg)
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();

        System.out.println("Round-1 output:\n" + firstOutput);
        String firstImageReference = extractImageReference(firstOutput);
        Assumptions.assumeTrue(firstImageReference != null && !firstImageReference.isBlank(), "No image reference found in round-1 output.");

        byte[] firstImage = loadImageBytes(firstImageReference);
        String firstExtension = inferExtension(firstImageReference);
        Path realisticOutputPath = Path.of("src/test/resources/outputs/images/flying-cat-in-sky-realistic." + firstExtension);
        Files.createDirectories(realisticOutputPath.getParent());
        Files.write(realisticOutputPath, firstImage);

        String secondOutput = runner.chatClient("image-cat-agent")
            .prompt()
            .user("Please change the style of the generated image to cartoon style.")
            .runConfiguration(cfg)
            .runHooks(ExampleSupport.noopHooks())
            .stream(false)
            .call()
            .content();

        System.out.println("Round-2 output:\n" + secondOutput);
        String secondImageReference = extractImageReference(secondOutput);
        Assumptions.assumeTrue(secondImageReference != null && !secondImageReference.isBlank(), "No image reference found in round-2 output.");

        byte[] secondImage = loadImageBytes(secondImageReference);
        String secondExtension = inferExtension(secondImageReference);
        Path cartoonOutputPath = Path.of("src/test/resources/outputs/images/flying-cat-in-sky-cartoon." + secondExtension);
        Files.createDirectories(cartoonOutputPath.getParent());
        Files.write(cartoonOutputPath, secondImage);

        Assertions.assertTrue(Files.size(realisticOutputPath) > 0);
        Assertions.assertTrue(Files.size(cartoonOutputPath) > 0);
        System.out.println("Saved realistic image to: " + realisticOutputPath.toAbsolutePath());
        System.out.println("Saved cartoon image to: " + cartoonOutputPath.toAbsolutePath());
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
