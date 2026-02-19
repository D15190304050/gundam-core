package stark.dataworks.coderaider.gundam.core.llmspi.adapter;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

/**
 * Configuration for OpenAI-compatible chat completion endpoints.
 */
@Getter
public class OpenAiCompatibleConfiguration
{
    private final String provider;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final Duration timeout;
    private final Map<String, String> headers;

    public OpenAiCompatibleConfiguration(String provider,
                                         String baseUrl,
                                         String apiKey,
                                         String model,
                                         Duration timeout,
                                         Map<String, String> headers)
    {
        this.provider = provider == null ? "openai-compatible" : provider;
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.model = Objects.requireNonNull(model, "model");
        this.timeout = timeout == null ? Duration.ofSeconds(60) : timeout;
        this.headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static OpenAiCompatibleConfiguration openAi(String apiKey, String model)
    {
        return new OpenAiCompatibleConfiguration("openai", "https://api.openai.com/v1", apiKey, model, Duration.ofSeconds(60), Map.of());
    }

    public static OpenAiCompatibleConfiguration gemini(String apiKey, String model)
    {
        return new OpenAiCompatibleConfiguration("gemini", "https://generativelanguage.googleapis.com/v1beta/openai", apiKey, model, Duration.ofSeconds(60), Map.of());
    }

    public static OpenAiCompatibleConfiguration qwen(String apiKey, String model)
    {
        return new OpenAiCompatibleConfiguration("qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1", apiKey, model, Duration.ofSeconds(60), Map.of());
    }

    public static OpenAiCompatibleConfiguration seed(String apiKey, String model)
    {
        return new OpenAiCompatibleConfiguration("seed", "https://ark.cn-beijing.volces.com/api/v3", apiKey, model, Duration.ofSeconds(60), Map.of());
    }

    public static OpenAiCompatibleConfiguration deepSeek(String apiKey, String model)
    {
        return new OpenAiCompatibleConfiguration("deepseek", "https://api.deepseek.com", apiKey, model, Duration.ofSeconds(60), Map.of());
    }
}
