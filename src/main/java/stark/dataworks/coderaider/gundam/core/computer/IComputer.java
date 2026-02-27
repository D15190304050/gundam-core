package stark.dataworks.coderaider.gundam.core.computer;

import java.util.List;

/**
 * A computer interface abstracting operations needed to control a computer or browser.
 * Implementations can provide sync or async operations for browser automation, desktop control, etc.
 */
public interface IComputer
{
    /**
     * Gets the environment type for this computer.
     * @return The environment (mac, windows, ubuntu, browser).
     */
    Environment getEnvironment();

    /**
     * Gets the display dimensions.
     * @return An array of [width, height] in pixels.
     */
    int[] getDimensions();

    /**
     * Takes a screenshot of the current screen.
     * @return Base64-encoded screenshot image.
     */
    String screenshot();

    /**
     * Performs a mouse click at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param button The mouse button to click.
     */
    void click(int x, int y, Button button);

    /**
     * Performs a mouse double-click at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    void doubleClick(int x, int y);

    /**
     * Performs a scroll operation at the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param scrollX The horizontal scroll amount.
     * @param scrollY The vertical scroll amount.
     */
    void scroll(int x, int y, int scrollX, int scrollY);

    /**
     * Types the specified text.
     * @param text The text to type.
     */
    void type(String text);

    /**
     * Waits for a short period (typically for UI updates).
     * Named waitAction to avoid conflict with Object.wait().
     */
    void waitAction();

    /**
     * Moves the mouse cursor to the specified coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    void move(int x, int y);

    /**
     * Presses the specified key combination.
     * @param keys The keys to press (e.g., ["ctrl", "c"]).
     */
    void keypress(List<String> keys);

    /**
     * Performs a drag operation along the specified path.
     * @param path The list of (x, y) coordinates defining the drag path.
     */
    void drag(List<int[]> path);
}
