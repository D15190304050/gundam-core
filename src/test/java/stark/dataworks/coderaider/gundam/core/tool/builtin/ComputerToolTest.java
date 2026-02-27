package stark.dataworks.coderaider.gundam.core.tool.builtin;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import stark.dataworks.coderaider.gundam.core.computer.Button;
import stark.dataworks.coderaider.gundam.core.computer.Environment;
import stark.dataworks.coderaider.gundam.core.computer.SimulatedComputer;

class ComputerToolTest
{
    @Test
    void testToolDefinition()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        assertEquals("computer_use_preview", tool.definition().getName());
        assertTrue(tool.definition().getDescription().contains("computer"));
        assertEquals(computer, tool.getComputer());
    }

    @Test
    void testScreenshot()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of("action", "screenshot");
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("screenshot"));
        assertEquals(1, computer.getScreenshotCount());
    }

    @Test
    void testClick()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "click",
            "x", 100,
            "y", 200,
            "button", "left"
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("click"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("click", actions.get(0).getAction());
        assertEquals(100, actions.get(0).getX());
        assertEquals(200, actions.get(0).getY());
        assertEquals(Button.LEFT, actions.get(0).getButton());
    }

    @Test
    void testDoubleClick()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "double_click",
            "x", 150,
            "y", 250
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("double_click"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("double_click", actions.get(0).getAction());
    }

    @Test
    void testScroll()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "scroll",
            "x", 100,
            "y", 200,
            "scroll_x", 10,
            "scroll_y", -50
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("scroll"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("scroll", actions.get(0).getAction());
        assertEquals(10, actions.get(0).getScrollX());
        assertEquals(-50, actions.get(0).getScrollY());
    }

    @Test
    void testType()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "type",
            "text", "Hello World"
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("type"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("type", actions.get(0).getAction());
        assertEquals("Hello World", actions.get(0).getText());
    }

    @Test
    void testWait()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of("action", "wait");
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("wait"));
    }

    @Test
    void testMove()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "move",
            "x", 300,
            "y", 400
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("move"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("move", actions.get(0).getAction());
        assertEquals(300, actions.get(0).getX());
        assertEquals(400, actions.get(0).getY());
    }

    @Test
    void testKeypress()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "keypress",
            "keys", List.of("ctrl", "c")
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("keypress"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("keypress", actions.get(0).getAction());
        assertEquals(List.of("ctrl", "c"), actions.get(0).getKeys());
    }

    @Test
    void testDrag()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of(
            "action", "drag",
            "path", List.of(
                List.of(100, 100),
                List.of(200, 200),
                List.of(300, 300)
            )
        );
        String result = tool.execute(input);

        assertTrue(result.contains("success"));
        assertTrue(result.contains("drag"));

        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        assertEquals(1, actions.size());
        assertEquals("drag", actions.get(0).getAction());
        assertEquals(3, actions.get(0).getPath().size());
    }

    @Test
    void testMissingAction()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        String result = tool.execute(Map.of());
        assertTrue(result.contains("error"));
    }

    @Test
    void testUnknownAction()
    {
        SimulatedComputer computer = new SimulatedComputer();
        ComputerTool tool = new ComputerTool(computer);

        Map<String, Object> input = Map.of("action", "unknown");
        String result = tool.execute(input);

        assertTrue(result.contains("error"));
    }

    @Test
    void testSimulatedComputerEnvironment()
    {
        SimulatedComputer computer = new SimulatedComputer(Environment.MAC, 1920, 1080);
        assertEquals(Environment.MAC, computer.getEnvironment());
        assertArrayEquals(new int[]{1920, 1080}, computer.getDimensions());
    }

    @Test
    void testSimulatedComputerClearActionLog()
    {
        SimulatedComputer computer = new SimulatedComputer();
        computer.screenshot();
        computer.screenshot();
        assertEquals(2, computer.getActionLog().size());

        computer.clearActionLog();
        assertEquals(0, computer.getActionLog().size());
        assertEquals(0, computer.getScreenshotCount());
    }
}
