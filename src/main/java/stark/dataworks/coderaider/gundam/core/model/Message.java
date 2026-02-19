package stark.dataworks.coderaider.gundam.core.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import stark.dataworks.coderaider.gundam.core.multimodal.MessagePart;
import stark.dataworks.coderaider.gundam.core.multimodal.MessagePartType;

/**
 * Message implements core runtime responsibilities.
 * It keeps this concern isolated so the kernel remains modular and provider-agnostic.
 */
@Getter
public class Message
{

    /**
     * Internal state for role; used while coordinating runtime behavior.
     */
    private final Role role;

    /**
     * Internal state for content; used while coordinating runtime behavior.
     */
    private final String content;

    /**
     * Internal state for parts; used while coordinating runtime behavior.
     */
    private final List<MessagePart> parts;

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param content The content used by this operation.
     */
    public Message(Role role, String content)
    {
        this(role, List.of(MessagePart.text(content)));
    }

    /**
     * Performs message as part of Message runtime responsibilities.
     * @param role The role used by this operation.
     * @param parts The parts used by this operation.
     */
    public Message(Role role, List<MessagePart> parts)
    {
        this.role = Objects.requireNonNull(role, "role");
        this.parts = Collections.unmodifiableList(Objects.requireNonNull(parts, "parts"));
        this.content = this.parts.stream()
            .filter(p -> p.getType() == MessagePartType.TEXT)
            .map(MessagePart::getText)
            .collect(Collectors.joining());
    }
}
