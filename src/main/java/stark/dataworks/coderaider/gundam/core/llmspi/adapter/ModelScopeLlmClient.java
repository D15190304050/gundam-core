package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import stark.dataworks.coderaider.gundam.core.llmspi.ILlmStreamListener;
import stark.dataworks.coderaider.gundam.core.llmspi.IMultimodalLlmClient;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmOptions;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmRequest;
import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.multimodal.AudioGenerationRequest;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAssetType;
import stark.dataworks.coderaider.gundam.core.multimodal.ImageGenerationRequest;
import stark.dataworks.coderaider.gundam.core.multimodal.VideoGenerationRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ModelScope adapter built on the OpenAI-compatible base client.
 * <p>
 * ModelScope provides OpenAI-compatible API for various models including Qwen.
 * Automatically adds ModelScope-specific options like enable_thinking for reasoning models.
 * Example usage:
 * <pre>
 * ModelScopeLlmClient client = new ModelScopeLlmClient("ms-xxx", "Qwen/Qwen3-4B");
 * </pre>
 */
public class ModelScopeLlmClient extends OpenAiCompatibleLlmClient implements IMultimodalLlmClient
{
    private static final String API_BASE_URL = "https://api-inference.modelscope.cn/v1";
    private static final Duration IMAGE_TIMEOUT = Duration.ofSeconds(180);

    private final boolean enableThinking;
    private final String apiKey;
    private final String defaultModel;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ModelScopeLlmClient(String apiKey, String model)
    {
        this(apiKey, model, true);
    }

    public ModelScopeLlmClient(String apiKey, String model, boolean enableThinking)
    {
        super(OpenAiCompatibleConfiguration.modelScope(apiKey, model));
        this.enableThinking = enableThinking;
        this.apiKey = apiKey;
        this.defaultModel = model;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse chat(LlmRequest request)
    {
        // enable_thinking is only supported in stream mode, must be false for non-stream calls
        return super.chat(enhanceRequestForNonStream(request));
    }

    @Override
    public LlmResponse chatStream(LlmRequest request, ILlmStreamListener listener)
    {
        return super.chatStream(enhanceRequestForStream(request), listener);
    }

    @Override
    public GeneratedAsset generate(ImageGenerationRequest request)
    {
        try
        {
            String model = request.getModel().isBlank() ? defaultModel : request.getModel();
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("prompt", request.getPrompt());
            payload.putAll(filterGenerationPayloadOptions(request.getProviderOptions()));
            if (!request.getSize().isBlank())
            {
                payload.put("size", request.getSize());
            }

            HttpRequest createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/images/generations"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("X-ModelScope-Async-Mode", "true")
                .timeout(IMAGE_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> createTaskResponse = httpClient.send(createTaskRequest, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(createTaskResponse.statusCode(), createTaskResponse.body());

            JsonNode createJson = objectMapper.readTree(createTaskResponse.body());
            String taskId = createJson.path("task_id").asText("");
            if (taskId.isBlank())
            {
                throw new IllegalStateException("Image generation task_id is empty. body=" + createTaskResponse.body());
            }

            int maxPollCount = intOption(request.getProviderOptions(), "maxPollCount", 36);
            int pollIntervalMillis = intOption(request.getProviderOptions(), "pollIntervalMillis", 5000);
            for (int i = 0; i < maxPollCount; i++)
            {
                HttpRequest pollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/tasks/" + taskId))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-ModelScope-Task-Type", "image_generation")
                    .timeout(IMAGE_TIMEOUT)
                    .GET()
                    .build();

                HttpResponse<String> pollResponse = httpClient.send(pollRequest, HttpResponse.BodyHandlers.ofString());
                ensureSuccess(pollResponse.statusCode(), pollResponse.body());

                JsonNode taskJson = objectMapper.readTree(pollResponse.body());
                String status = taskJson.path("task_status").asText("");
                if ("SUCCEED".equalsIgnoreCase(status))
                {
                    JsonNode imagesNode = taskJson.path("output_images");
                    if (!imagesNode.isArray() || imagesNode.isEmpty())
                    {
                        throw new IllegalStateException("Image generation succeeded but output_images is empty. body=" + pollResponse.body());
                    }

                    String imageUri = imagesNode.get(0).asText("");
                    if (imageUri.isBlank())
                    {
                        throw new IllegalStateException("Image generation returned an empty output image URI. body=" + pollResponse.body());
                    }
                    return new GeneratedAsset(GeneratedAssetType.IMAGE, imageUri, "image/png", Map.of(
                        "provider", "modelscope",
                        "model", model,
                        "taskId", taskId,
                        "taskStatus", status));
                }

                if ("FAILED".equalsIgnoreCase(status))
                {
                    throw new IllegalStateException("Image generation task failed. body=" + pollResponse.body());
                }

                Thread.sleep(pollIntervalMillis);
            }

            throw new IllegalStateException("Image generation task timed out. taskId=" + taskId + ".");
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to generate image via ModelScope", ex);
        }
    }

    @Override
    public GeneratedAsset generate(VideoGenerationRequest request)
    {
        throw new UnsupportedOperationException("ModelScope video generation is not implemented yet.");
    }

    @Override
    public GeneratedAsset generate(AudioGenerationRequest request)
    {
        throw new UnsupportedOperationException("ModelScope audio generation is not implemented yet.");
    }


    private static Map<String, Object> filterGenerationPayloadOptions(Map<String, Object> options)
    {
        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : options.entrySet())
        {
            if ("pollIntervalMillis".equals(entry.getKey()) || "maxPollCount".equals(entry.getKey()))
            {
                continue;
            }
            filtered.put(entry.getKey(), entry.getValue());
        }
        return filtered;
    }

    private static int intOption(Map<String, Object> options, String key, int defaultValue)
    {
        Object value = options.get(key);
        if (value instanceof Number number)
        {
            return number.intValue();
        }
        if (value instanceof String text)
        {
            try
            {
                return Integer.parseInt(text);
            }
            catch (NumberFormatException ignored)
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static void ensureSuccess(int statusCode, String body)
    {
        if (statusCode < 200 || statusCode >= 300)
        {
            throw new IllegalStateException("Provider call failed with status=" + statusCode + ", body=" + body);
        }
    }

    private LlmRequest enhanceRequestForNonStream(LlmRequest request)
    {
        Map<String, Object> providerOptions = new HashMap<>(request.getOptions().getProviderOptions());
        // enable_thinking must be false for non-stream calls
        providerOptions.put("enable_thinking", false);

        LlmOptions enhancedOptions = new LlmOptions(
            request.getOptions().getTemperature(),
            request.getOptions().getMaxTokens(),
            request.getOptions().getToolChoice(),
            request.getOptions().getResponseFormat(),
            providerOptions
        );

        return new LlmRequest(
            request.getModel(),
            request.getMessages(),
            request.getTools(),
            enhancedOptions,
            request.getAttachments()
        );
    }

    private LlmRequest enhanceRequestForStream(LlmRequest request)
    {
        Map<String, Object> providerOptions = new HashMap<>(request.getOptions().getProviderOptions());
        // enable_thinking is only supported in stream mode
        providerOptions.put("enable_thinking", enableThinking);

        LlmOptions enhancedOptions = new LlmOptions(
            request.getOptions().getTemperature(),
            request.getOptions().getMaxTokens(),
            request.getOptions().getToolChoice(),
            request.getOptions().getResponseFormat(),
            providerOptions
        );

        return new LlmRequest(
            request.getModel(),
            request.getMessages(),
            request.getTools(),
            enhancedOptions,
            request.getAttachments()
        );
    }
}
