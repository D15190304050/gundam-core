package stark.dataworks.coderaider.genericagent.core.oss;

/**
 * IOssClient abstracts object storage operations for generated multimodal assets.
 */
public interface IOssClient
{
    /**
     * Puts an object into the object storage.
     *
     * @param key          object key.
     * @param payload      object payload.
     * @param contentType  object content type.
     * @return Public URL of the object.
     */
    String putObject(String key, byte[] payload, String contentType);

    String getPublicUrl(String key);
}
