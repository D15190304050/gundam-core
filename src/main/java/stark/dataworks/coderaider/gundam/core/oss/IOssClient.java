package stark.dataworks.coderaider.gundam.core.oss;

/**
 * IOssClient abstracts object storage operations for generated multimodal assets.
 */
public interface IOssClient
{
    String putObject(String key, byte[] payload, String contentType);

    String getPublicUrl(String key);
}
