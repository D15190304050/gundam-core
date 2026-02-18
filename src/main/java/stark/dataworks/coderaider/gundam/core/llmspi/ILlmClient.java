package stark.dataworks.coderaider.gundam.core.llmspi;

public interface ILlmClient {
    LlmResponse chat(LlmRequest request);
}
