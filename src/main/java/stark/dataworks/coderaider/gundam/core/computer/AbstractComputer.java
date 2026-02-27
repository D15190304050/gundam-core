package stark.dataworks.coderaider.gundam.core.computer;

import java.util.List;

/**
 * Abstract base implementation of IComputer providing common functionality.
 * Subclasses implement the actual platform-specific operations.
 */
public abstract class AbstractComputer implements IComputer
{
    private final Environment environment;
    private final int displayWidth;
    private final int displayHeight;

    protected AbstractComputer(Environment environment, int displayWidth, int displayHeight)
    {
        this.environment = environment;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public Environment getEnvironment()
    {
        return environment;
    }

    @Override
    public int[] getDimensions()
    {
        return new int[]{displayWidth, displayHeight};
    }

    @Override
    public void click(int x, int y, Button button)
    {
        validateCoordinates(x, y);
        doClick(x, y, button);
    }

    @Override
    public void doubleClick(int x, int y)
    {
        validateCoordinates(x, y);
        doDoubleClick(x, y);
    }

    @Override
    public void scroll(int x, int y, int scrollX, int scrollY)
    {
        validateCoordinates(x, y);
        doScroll(x, y, scrollX, scrollY);
    }

    @Override
    public void type(String text)
    {
        if (text == null || text.isEmpty())
        {
            return;
        }
        doType(text);
    }

    @Override
    public void waitAction()
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void move(int x, int y)
    {
        validateCoordinates(x, y);
        doMove(x, y);
    }

    @Override
    public void keypress(List<String> keys)
    {
        if (keys == null || keys.isEmpty())
        {
            return;
        }
        doKeypress(keys);
    }

    @Override
    public void drag(List<int[]> path)
    {
        if (path == null || path.isEmpty())
        {
            return;
        }
        for (int[] point : path)
        {
            if (point.length >= 2)
            {
                validateCoordinates(point[0], point[1]);
            }
        }
        doDrag(path);
    }

    protected void validateCoordinates(int x, int y)
    {
        if (x < 0 || x >= displayWidth)
        {
            throw new IllegalArgumentException("x coordinate out of bounds: " + x);
        }
        if (y < 0 || y >= displayHeight)
        {
            throw new IllegalArgumentException("y coordinate out of bounds: " + y);
        }
    }

    protected abstract void doClick(int x, int y, Button button);

    protected abstract void doDoubleClick(int x, int y);

    protected abstract void doScroll(int x, int y, int scrollX, int scrollY);

    protected abstract void doType(String text);

    protected abstract void doMove(int x, int y);

    protected abstract void doKeypress(List<String> keys);

    protected abstract void doDrag(List<int[]> path);
}
