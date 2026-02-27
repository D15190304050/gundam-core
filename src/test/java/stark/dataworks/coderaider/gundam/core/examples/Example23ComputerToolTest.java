package stark.dataworks.coderaider.gundam.core.examples;

import java.util.List;

import org.junit.jupiter.api.Test;

import stark.dataworks.coderaider.gundam.core.computer.Environment;
import stark.dataworks.coderaider.gundam.core.computer.SimulatedComputer;
import stark.dataworks.coderaider.gundam.core.tool.builtin.ComputerTool;

/**
 * Example demonstrating ComputerTool for browser/desktop automation.
 * The tool supports screenshot, click, double_click, scroll, type, wait, move, keypress, and drag operations.
 */
public class Example23ComputerToolTest
{
    @Test
    public void run()
    {
        System.out.println("=== ComputerTool Example ===");

        SimulatedComputer computer = new SimulatedComputer(Environment.BROWSER, 1024, 768);
        ComputerTool tool = new ComputerTool(computer);

        System.out.println("\nTool name: " + tool.definition().getName());
        System.out.println("Tool description: " + tool.definition().getDescription());
        System.out.println("Computer environment: " + computer.getEnvironment());
        System.out.println("Display dimensions: " + computer.getDimensions()[0] + "x" + computer.getDimensions()[1]);

        System.out.println("\n=== Testing Operations ===");

        System.out.println("\n1. Screenshot:");
        String screenshotResult = tool.execute(java.util.Map.of("action", "screenshot"));
        System.out.println("   Result: " + screenshotResult);

        System.out.println("\n2. Click:");
        String clickResult = tool.execute(java.util.Map.of(
            "action", "click",
            "x", 100,
            "y", 200,
            "button", "left"
        ));
        System.out.println("   Result: " + clickResult);

        System.out.println("\n3. Double Click:");
        String doubleClickResult = tool.execute(java.util.Map.of(
            "action", "double_click",
            "x", 150,
            "y", 250
        ));
        System.out.println("   Result: " + doubleClickResult);

        System.out.println("\n4. Type:");
        String typeResult = tool.execute(java.util.Map.of(
            "action", "type",
            "text", "Hello, World!"
        ));
        System.out.println("   Result: " + typeResult);

        System.out.println("\n5. Scroll:");
        String scrollResult = tool.execute(java.util.Map.of(
            "action", "scroll",
            "x", 500,
            "y", 400,
            "scroll_x", 0,
            "scroll_y", -100
        ));
        System.out.println("   Result: " + scrollResult);

        System.out.println("\n6. Move:");
        String moveResult = tool.execute(java.util.Map.of(
            "action", "move",
            "x", 300,
            "y", 300
        ));
        System.out.println("   Result: " + moveResult);

        System.out.println("\n7. Keypress:");
        String keypressResult = tool.execute(java.util.Map.of(
            "action", "keypress",
            "keys", List.of("ctrl", "c")
        ));
        System.out.println("   Result: " + keypressResult);

        System.out.println("\n8. Drag:");
        String dragResult = tool.execute(java.util.Map.of(
            "action", "drag",
            "path", List.of(
                List.of(100, 100),
                List.of(200, 200),
                List.of(300, 300)
            )
        ));
        System.out.println("   Result: " + dragResult);

        System.out.println("\n9. Wait:");
        String waitResult = tool.execute(java.util.Map.of("action", "wait"));
        System.out.println("   Result: " + waitResult);

        System.out.println("\n=== Action Log ===");
        List<SimulatedComputer.ComputerAction> actions = computer.getActionLog();
        for (int i = 0; i < actions.size(); i++)
        {
            System.out.println((i + 1) + ". " + actions.get(i));
        }

        System.out.println("\n=== Screenshot Count ===");
        System.out.println("Total screenshots: " + computer.getScreenshotCount());

        System.out.println("\nExample completed successfully!");
    }
}
