package stark.dataworks.coderaider.gundam.core.multimodal;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * MessagePart represents one multimodal message unit (text, image, video, or audio).
 */
@Getter
public class MessagePart
{
    private final MessagePartType type;
    private final String text;
    private final String uri;
    private final String mimeType;
    private final Map<String, Object> metadata;

    public MessagePart(MessagePartType type, String text, String uri, String mimeType, Map<String, Object> metadata)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.text = text == null ? "" : text;
        this.uri = uri == null ? "" : uri;
        this.mimeType = mimeType == null ? "" : mimeType;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }

    public static MessagePart text(String value)
    {
        return new MessagePart(MessagePartType.TEXT, value, "", "text/plain", Map.of());
    }

    public static MessagePart image(String uri, String mimeType)
    {
        return new MessagePart(MessagePartType.IMAGE, "", uri, mimeType, Map.of());
    }

    public static MessagePart video(String uri, String mimeType)
    {
        return new MessagePart(MessagePartType.VIDEO, "", uri, mimeType, Map.of());
    }

    public static MessagePart audio(String uri, String mimeType)
    {
        return new MessagePart(MessagePartType.AUDIO, "", uri, mimeType, Map.of());
    }
}
