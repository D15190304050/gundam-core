package stark.dataworks.coderaider.llmspi;

public interface ILlmClient {
    LlmResponse chat(LlmRequest request);
}
