package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import stark.dataworks.coderaider.gundam.core.computer.Button;
import stark.dataworks.coderaider.gundam.core.computer.IComputer;
import stark.dataworks.coderaider.gundam.core.tool.ITool;
import stark.dataworks.coderaider.gundam.core.tool.ToolDefinition;
import stark.dataworks.coderaider.gundam.core.tool.ToolParameterSchema;

/**
 * ComputerTool is a hosted tool that lets the LLM control a computer.
 * Supports screenshot, click, double_click, scroll, type, wait, move, keypress, and drag operations.
 */
public class ComputerTool implements ITool
{
    private static final String TOOL_NAME = "computer_use_preview";
    private static final String TOOL_DESCRIPTION = "Control a computer with mouse and keyboard operations.";

    private final IComputer computer;
    private final ToolDefinition definition;

    public ComputerTool(IComputer computer)
    {
        this.computer = computer;
        this.definition = createDefinition();
    }

    private ToolDefinition createDefinition()
    {
        return new ToolDefinition(
            TOOL_NAME,
            TOOL_DESCRIPTION,
            List.of(
                new ToolParameterSchema("action", "string", true, "The action to perform: screenshot, click, double_click, scroll, type, wait, move, keypress, drag"),
                new ToolParameterSchema("x", "integer", false, "X coordinate for mouse operations"),
                new ToolParameterSchema("y", "integer", false, "Y coordinate for mouse operations"),
                new ToolParameterSchema("button", "string", false, "Mouse button: left, right, wheel, back, forward"),
                new ToolParameterSchema("scroll_x", "integer", false, "Horizontal scroll amount"),
                new ToolParameterSchema("scroll_y", "integer", false, "Vertical scroll amount"),
                new ToolParameterSchema("text", "string", false, "Text to type"),
                new ToolParameterSchema("keys", "array", false, "Keys to press (e.g., ['ctrl', 'c'])"),
                new ToolParameterSchema("path", "array", false, "Drag path as list of [x, y] coordinates")
            )
        );
    }

    @Override
    public ToolDefinition definition()
    {
        return definition;
    }

    @Override
    public String execute(Map<String, Object> input)
    {
        if (computer == null)
        {
            return errorResult("Computer not initialized");
        }

        String action = getStringValue(input, "action");
        if (action == null || action.isEmpty())
        {
            return errorResult("Missing 'action' parameter");
        }

        try
        {
            switch (action.toLowerCase())
            {
                case "screenshot":
                    return handleScreenshot();

                case "click":
                    return handleClick(input);

                case "double_click":
                    return handleDoubleClick(input);

                case "scroll":
                    return handleScroll(input);

                case "type":
                    return handleType(input);

                case "wait":
                    return handleWait();

                case "move":
                    return handleMove(input);

                case "keypress":
                    return handleKeypress(input);

                case "drag":
                    return handleDrag(input);

                default:
                    return errorResult("Unknown action: " + action);
            }
        }
        catch (Exception e)
        {
            return errorResult("Exception during " + action + ": " + e.getMessage());
        }
    }

    public IComputer getComputer()
    {
        return computer;
    }

    private String handleScreenshot()
    {
        String screenshot = computer.screenshot();
        return successResult("screenshot", screenshot);
    }

    private String handleClick(Map<String, Object> input)
    {
        int x = getIntValue(input, "x", -1);
        int y = getIntValue(input, "y", -1);
        String buttonStr = getStringValue(input, "button", "left");
        Button button = parseButton(buttonStr);

        computer.click(x, y, button);
        return successResult("click", "Clicked at (" + x + ", " + y + ") with " + button + " button");
    }

    private String handleDoubleClick(Map<String, Object> input)
    {
        int x = getIntValue(input, "x", -1);
        int y = getIntValue(input, "y", -1);

        computer.doubleClick(x, y);
        return successResult("double_click", "Double-clicked at (" + x + ", " + y + ")");
    }

    private String handleScroll(Map<String, Object> input)
    {
        int x = getIntValue(input, "x", 0);
        int y = getIntValue(input, "y", 0);
        int scrollX = getIntValue(input, "scroll_x", 0);
        int scrollY = getIntValue(input, "scroll_y", 0);

        computer.scroll(x, y, scrollX, scrollY);
        return successResult("scroll", "Scrolled at (" + x + ", " + y + ") by (" + scrollX + ", " + scrollY + ")");
    }

    private String handleType(Map<String, Object> input)
    {
        String text = getStringValue(input, "text", "");
        computer.type(text);
        return successResult("type", "Typed: " + (text.length() > 50 ? text.substring(0, 50) + "..." : text));
    }

    private String handleWait()
    {
        computer.waitAction();
        return successResult("wait", "Waited");
    }

    private String handleMove(Map<String, Object> input)
    {
        int x = getIntValue(input, "x", -1);
        int y = getIntValue(input, "y", -1);

        computer.move(x, y);
        return successResult("move", "Moved to (" + x + ", " + y + ")");
    }

    private String handleKeypress(Map<String, Object> input)
    {
        List<String> keys = getStringListValue(input, "keys");
        computer.keypress(keys);
        return successResult("keypress", "Pressed keys: " + String.join("+", keys));
    }

    private String handleDrag(Map<String, Object> input)
    {
        List<int[]> path = getPathValue(input, "path");
        computer.drag(path);
        return successResult("drag", "Dragged along path with " + path.size() + " points");
    }

    private Button parseButton(String buttonStr)
    {
        if (buttonStr == null)
        {
            return Button.LEFT;
        }
        try
        {
            return Button.valueOf(buttonStr.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return Button.LEFT;
        }
    }

    private String getStringValue(Map<String, Object> input, String key)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return null;
        }
        return String.valueOf(value);
    }

    private String getStringValue(Map<String, Object> input, String key, String defaultValue)
    {
        String value = getStringValue(input, key);
        return value != null ? value : defaultValue;
    }

    private int getIntValue(Map<String, Object> input, String key, int defaultValue)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringListValue(Map<String, Object> input, String key)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return new ArrayList<>();
        }
        if (value instanceof List)
        {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value)
            {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<int[]> getPathValue(Map<String, Object> input, String key)
    {
        Object value = input.get(key);
        if (value == null)
        {
            return new ArrayList<>();
        }
        if (value instanceof List)
        {
            List<int[]> result = new ArrayList<>();
            for (Object item : (List<?>) value)
            {
                if (item instanceof List)
                {
                    List<?> coords = (List<?>) item;
                    if (coords.size() >= 2)
                    {
                        int x = coords.get(0) instanceof Number ? ((Number) coords.get(0)).intValue() : 0;
                        int y = coords.get(1) instanceof Number ? ((Number) coords.get(1)).intValue() : 0;
                        result.add(new int[]{x, y});
                    }
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private String successResult(String action, String message)
    {
        return "{\"status\": \"success\", \"action\": \"" + action + "\", \"message\": " + toJsonString(message) + "}";
    }

    private String errorResult(String error)
    {
        return "{\"status\": \"error\", \"error\": " + toJsonString(error) + "}";
    }

    private String toJsonString(String value)
    {
        if (value == null)
        {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}
