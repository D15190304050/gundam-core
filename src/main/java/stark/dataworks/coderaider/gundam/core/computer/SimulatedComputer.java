package stark.dataworks.coderaider.gundam.core.computer;

import java.util.ArrayList;
import java.util.List;

/**
 * A simulated computer implementation for testing purposes.
 * Records all actions performed and returns simulated screenshots.
 */
public class SimulatedComputer extends AbstractComputer
{
    private final List<ComputerAction> actionLog;
    private int screenshotCount;

    public SimulatedComputer()
    {
        this(Environment.BROWSER, 1024, 768);
    }

    public SimulatedComputer(Environment environment, int width, int height)
    {
        super(environment, width, height);
        this.actionLog = new ArrayList<>();
        this.screenshotCount = 0;
    }

    @Override
    public String screenshot()
    {
        screenshotCount++;
        actionLog.add(ComputerAction.forScreenshot());
        return "simulated_screenshot_" + screenshotCount + "_base64";
    }

    @Override
    protected void doClick(int x, int y, Button button)
    {
        actionLog.add(ComputerAction.forClick(x, y, button));
    }

    @Override
    protected void doDoubleClick(int x, int y)
    {
        actionLog.add(ComputerAction.forDoubleClick(x, y));
    }

    @Override
    protected void doScroll(int x, int y, int scrollX, int scrollY)
    {
        actionLog.add(ComputerAction.forScroll(x, y, scrollX, scrollY));
    }

    @Override
    protected void doType(String text)
    {
        actionLog.add(ComputerAction.forType(text));
    }

    @Override
    protected void doMove(int x, int y)
    {
        actionLog.add(ComputerAction.forMove(x, y));
    }

    @Override
    protected void doKeypress(List<String> keys)
    {
        actionLog.add(ComputerAction.forKeypress(keys));
    }

    @Override
    protected void doDrag(List<int[]> path)
    {
        actionLog.add(ComputerAction.forDrag(path));
    }

    public List<ComputerAction> getActionLog()
    {
        return new ArrayList<>(actionLog);
    }

    public void clearActionLog()
    {
        actionLog.clear();
        screenshotCount = 0;
    }

    public int getScreenshotCount()
    {
        return screenshotCount;
    }

    public static class ComputerAction
    {
        private final String action;
        private final int x;
        private final int y;
        private final Button button;
        private final int scrollX;
        private final int scrollY;
        private final String text;
        private final List<String> keys;
        private final List<int[]> path;

        private ComputerAction(String action, int x, int y, Button button, int scrollX, int scrollY, String text, List<String> keys, List<int[]> path)
        {
            this.action = action;
            this.x = x;
            this.y = y;
            this.button = button;
            this.scrollX = scrollX;
            this.scrollY = scrollY;
            this.text = text;
            this.keys = keys != null ? new ArrayList<>(keys) : null;
            this.path = path != null ? new ArrayList<>(path) : null;
        }

        public static ComputerAction forScreenshot()
        {
            return new ComputerAction("screenshot", -1, -1, null, 0, 0, null, null, null);
        }

        public static ComputerAction forClick(int x, int y, Button button)
        {
            return new ComputerAction("click", x, y, button, 0, 0, null, null, null);
        }

        public static ComputerAction forDoubleClick(int x, int y)
        {
            return new ComputerAction("double_click", x, y, null, 0, 0, null, null, null);
        }

        public static ComputerAction forScroll(int x, int y, int scrollX, int scrollY)
        {
            return new ComputerAction("scroll", x, y, null, scrollX, scrollY, null, null, null);
        }

        public static ComputerAction forType(String text)
        {
            return new ComputerAction("type", -1, -1, null, 0, 0, text, null, null);
        }

        public static ComputerAction forMove(int x, int y)
        {
            return new ComputerAction("move", x, y, null, 0, 0, null, null, null);
        }

        public static ComputerAction forKeypress(List<String> keys)
        {
            return new ComputerAction("keypress", -1, -1, null, 0, 0, null, keys, null);
        }

        public static ComputerAction forDrag(List<int[]> path)
        {
            return new ComputerAction("drag", -1, -1, null, 0, 0, null, null, path);
        }

        public String getAction()
        {
            return action;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public Button getButton()
        {
            return button;
        }

        public int getScrollX()
        {
            return scrollX;
        }

        public int getScrollY()
        {
            return scrollY;
        }

        public String getText()
        {
            return text;
        }

        public List<String> getKeys()
        {
            return keys;
        }

        public List<int[]> getPath()
        {
            return path;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("ComputerAction{");
            sb.append("action='").append(action).append('\'');
            if (x >= 0)
            {
                sb.append(", x=").append(x);
            }
            if (y >= 0)
            {
                sb.append(", y=").append(y);
            }
            if (button != null)
            {
                sb.append(", button=").append(button);
            }
            if (scrollX != 0 || scrollY != 0)
            {
                sb.append(", scrollX=").append(scrollX).append(", scrollY=").append(scrollY);
            }
            if (text != null)
            {
                sb.append(", text='").append(text).append('\'');
            }
            if (keys != null)
            {
                sb.append(", keys=").append(keys);
            }
            if (path != null)
            {
                sb.append(", path=").append(path.size()).append(" points");
            }
            sb.append('}');
            return sb.toString();
        }
    }
}
