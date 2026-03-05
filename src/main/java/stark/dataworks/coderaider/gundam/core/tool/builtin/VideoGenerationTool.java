package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.Map;
import java.util.Objects;

import stark.dataworks.coderaider.gundam.core.multimodal.AudioGenerationRequest;
import stark.dataworks.coderaider.gundam.core.multimodal.GeneratedAsset;
import stark.dataworks.coderaider.gundam.core.multimodal.IAudioGenerator;
import stark.dataworks.coderaider.gundam.core.multimodal.IVideoGenerator;
import stark.dataworks.coderaider.gundam.core.multimodal.VideoGenerationRequest;
import stark.dataworks.coderaider.gundam.core.tool.ToolCategory;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;

/**
 * VideoGenerationTool implements tool contracts, schema metadata, and executable tool registration.
 */
public class VideoGenerationTool extends AbstractBuiltinTool
{
    private final IVideoGenerator videoGenerator;
    private final IAudioGenerator audioGenerator;

    /**
     * Initializes VideoGenerationTool with required runtime dependencies and options.
     * @param definition definition object.
     */
    public VideoGenerationTool(ToolDefinition definition)
    {
        this(definition, null, null);
    }

    /**
     * Initializes VideoGenerationTool with required runtime dependencies and options.
     * @param definition definition object.
     * @param videoGenerator video generator.
     * @param audioGenerator audio generator.
     */
    public VideoGenerationTool(ToolDefinition definition, IVideoGenerator videoGenerator, IAudioGenerator audioGenerator)
    {
        super(definition, ToolCategory.VIDEO_GENERATION);
        this.videoGenerator = videoGenerator;
        this.audioGenerator = audioGenerator;
    }

    /**
     * Executes this tool operation and returns the produced output.
     * @param Map<String map<string.
     * @param input input payload.
     * @return Tool execution output returned by the MCP server.
     */
    @Override
    public String execute(Map<String, Object> input)
    {
        String prompt = Objects.toString(input.getOrDefault("prompt", ""), "");
        if (videoGenerator == null)
        {
            return "VideoGeneration(simulated): prompt=" + prompt;
        }

        int durationSeconds = Integer.parseInt(Objects.toString(input.getOrDefault("durationSeconds", "8"), "8"));
        VideoGenerationRequest request = new VideoGenerationRequest(
            prompt,
            Objects.toString(input.getOrDefault("model", ""), ""),
            durationSeconds,
            Objects.toString(input.getOrDefault("aspectRatio", "16:9"), "16:9"),
            Map.of());

        GeneratedAsset video = videoGenerator.generate(request);
        GeneratedAsset audio = null;

        boolean generateAudio = Boolean.parseBoolean(Objects.toString(input.getOrDefault("withAudio", false), "false"));
        if (generateAudio && audioGenerator != null)
        {
            audio = audioGenerator.generate(new AudioGenerationRequest(
                prompt,
                Objects.toString(input.getOrDefault("audioModel", ""), ""),
                Objects.toString(input.getOrDefault("voice", ""), ""),
                Objects.toString(input.getOrDefault("audioFormat", "mp3"), "mp3"),
                Map.of()));
        }

        return "VideoGeneration: prompt=" + prompt + ", videoUri=" + video.getUri() +
            (audio == null ? "" : ", audioUri=" + audio.getUri());
    }
}
