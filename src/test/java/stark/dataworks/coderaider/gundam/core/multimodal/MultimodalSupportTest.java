package stark.dataworks.coderaider.gundam.core.multimodal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.llmspi.LlmResponse;
import stark.dataworks.coderaider.gundam.core.metrics.TokenUsage;
import stark.dataworks.coderaider.gundam.core.model.Message;
import stark.dataworks.coderaider.gundam.core.model.Role;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ImageGenerationTool;
import stark.dataworks.coderaider.gundam.core.tool.builtin.VideoGenerationTool;

class MultimodalSupportTest
{
    @Test
    void supportsMessageWithMixedTextAndMediaParts()
    {
        Message message = new Message(Role.USER, List.of(
            MessagePart.text("Describe this photo: "),
            MessagePart.image("https://example.com/photo.png", "image/png")));

        assertEquals("Describe this photo: ", message.getContent());
        assertEquals(2, message.getParts().size());
        assertEquals(MessagePartType.IMAGE, message.getParts().get(1).getType());
    }

    @Test
    void preservesGeneratedAssetsOnLlmResponse()
    {
        GeneratedAsset generatedAsset = new GeneratedAsset(
            GeneratedAssetType.IMAGE,
            "https://cdn.example.com/generated/a.png",
            "image/png",
            Map.of("width", 1024));

        LlmResponse response = new LlmResponse(
            "",
            List.of(),
            null,
            new TokenUsage(1, 1),
            "stop",
            Map.of(),
            List.of(generatedAsset));

        assertEquals(1, response.getGeneratedAssets().size());
        assertEquals("https://cdn.example.com/generated/a.png", response.getGeneratedAssets().get(0).getUri());
    }

    @Test
    void executesImageAndVideoToolsUsingProviderInterfaces()
    {
        ImageGenerationTool imageTool = new ImageGenerationTool(
            new ToolDefinition("image", "", List.of()),
            request -> new GeneratedAsset(GeneratedAssetType.IMAGE, "https://images.example.com/generated.png", "image/png", Map.of()),
            null);

        VideoGenerationTool videoTool = new VideoGenerationTool(
            new ToolDefinition("video", "", List.of()),
            request -> new GeneratedAsset(GeneratedAssetType.VIDEO, "https://video.example.com/generated.mp4", "video/mp4", Map.of()),
            request -> new GeneratedAsset(GeneratedAssetType.AUDIO, "https://audio.example.com/generated.mp3", "audio/mpeg", Map.of()));

        String imageResult = imageTool.execute(Map.of("prompt", "A gundam in space"));
        String videoResult = videoTool.execute(Map.of("prompt", "A mecha launch", "withAudio", true));

        assertTrue(imageResult.contains("https://images.example.com/generated.png"));
        assertTrue(videoResult.contains("https://video.example.com/generated.mp4"));
        assertTrue(videoResult.contains("https://audio.example.com/generated.mp3"));
    }
}
