package stark.dataworks.coderaider.gundam.core.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import stark.dataworks.coderaider.gundam.core.events.RunEvent;
import stark.dataworks.coderaider.gundam.core.events.RunEventType;
import stark.dataworks.coderaider.gundam.core.streaming.IRunEventListener;
import stark.dataworks.coderaider.gundam.core.streaming.RunEventPublisher;

final class ExampleStreamingPublishers
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ExampleStreamingPublishers()
    {
    }

    static RunEventPublisher textOnly()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new TextDeltaListener());
        return publisher;
    }

    static RunEventPublisher textWithToolLifecycle(String toolPrefix)
    {
        RunEventPublisher publisher = textOnly();
        publisher.subscribe(new ToolLifecycleListener(toolPrefix));
        return publisher;
    }

    static RunEventPublisher reasoningAndText()
    {
        RunEventPublisher publisher = textOnly();
        publisher.subscribe(event -> printReasoningDelta(event, "[reasoning] ", "\n"));
        return publisher;
    }

    static RunEventPublisher reasoningAndTextWithSections()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new ReasoningSectionListener());
        return publisher;
    }

    static RunEventPublisher reactThoughtActionObservation()
    {
        RunEventPublisher publisher = new RunEventPublisher();
        publisher.subscribe(new ReActTraceListener());
        return publisher;
    }

    private static String toJsonOrString(Object value)
    {
        if (value == null)
        {
            return "null";
        }
        try
        {
            Object normalized = value instanceof String ? OBJECT_MAPPER.readValue((String) value, Object.class) : value;
            return OBJECT_MAPPER.writeValueAsString(normalized);
        }
        catch (Exception e)
        {
            return String.valueOf(value);
        }
    }

    private static String compact(Object value)
    {
        String text = toJsonOrString(value);
        if (text == null)
        {
            return "null";
        }
        String normalized = text.replace("\r", " ").replace("\n", " ").trim();
        int maxLen = 240;
        if (normalized.length() <= maxLen)
        {
            return normalized;
        }
        return normalized.substring(0, maxLen) + "...(truncated)";
    }

    private static void printTextDelta(RunEvent event)
    {
        if (event.getType() != RunEventType.MODEL_RESPONSE_DELTA)
        {
            return;
        }
        String delta = (String) event.getAttributes().get("delta");
        if (delta != null && !delta.isEmpty())
        {
            System.out.print(delta);
            System.out.flush();
        }
    }

    private static void printReasoningDelta(RunEvent event, String prefix, String suffix)
    {
        if (event.getType() != RunEventType.MODEL_REASONING_DELTA)
        {
            return;
        }
        String delta = (String) event.getAttributes().get("delta");
        if (delta != null && !delta.isEmpty())
        {
            System.out.print(prefix + delta + suffix);
            System.out.flush();
        }
    }

    private static final class TextDeltaListener implements IRunEventListener
    {
        @Override
        public void onEvent(RunEvent event)
        {
            printTextDelta(event);
        }
    }

    private static final class ToolLifecycleListener implements IRunEventListener
    {
        private final String toolPrefix;

        private ToolLifecycleListener(String toolPrefix)
        {
            this.toolPrefix = toolPrefix == null ? "" : toolPrefix;
        }

        @Override
        public void onEvent(RunEvent event)
        {
            if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object args = event.getAttributes().get("arguments");
                System.out.println("\n[" + toolPrefix + "Tool call: " + tool + " with arguments: " + compact(args) + "]");
            }
            else if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object result = event.getAttributes().get("result");
                System.out.println("[" + toolPrefix + "Tool completed: " + tool + " with result: " + compact(result) + "]");
            }
        }
    }

    private static final class ReActTraceListener implements IRunEventListener
    {
        private boolean thoughtHeaderPrinted;
        private boolean actionHeaderPrinted;
        private boolean observationHeaderPrinted;
        private boolean answerHeaderPrinted;

        @Override
        public void onEvent(RunEvent event)
        {
            if (event.getType() == RunEventType.MODEL_REASONING_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!thoughtHeaderPrinted)
                    {
                        System.out.println("\n[Thought]");
                        thoughtHeaderPrinted = true;
                        actionHeaderPrinted = false;
                        observationHeaderPrinted = false;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
                return;
            }

            if (event.getType() == RunEventType.TOOL_CALL_REQUESTED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object args = event.getAttributes().get("arguments");
                if (!actionHeaderPrinted)
                {
                    System.out.println("\n\n[Action]");
                    actionHeaderPrinted = true;
                }
                System.out.println("tool=" + tool + " args=" + toJsonOrString(args));
                observationHeaderPrinted = false;
                return;
            }

            if (event.getType() == RunEventType.TOOL_CALL_COMPLETED)
            {
                String tool = (String) event.getAttributes().get("tool");
                Object result = event.getAttributes().get("result");
                if (!observationHeaderPrinted)
                {
                    System.out.println("[Observation]");
                    observationHeaderPrinted = true;
                }
                System.out.println("tool=" + tool + " result=" + toJsonOrString(result));
                thoughtHeaderPrinted = false;
                return;
            }

            if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!answerHeaderPrinted)
                    {
                        System.out.println("\n\n[Answer]");
                        answerHeaderPrinted = true;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
            }
        }
    }

    private static final class ReasoningSectionListener implements IRunEventListener
    {
        private boolean hasReasoning;
        private boolean hasAnswer;

        @Override
        public void onEvent(RunEvent event)
        {
            if (event.getType() == RunEventType.MODEL_REASONING_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!hasReasoning)
                    {
                        System.out.println("=== Thinking ===");
                        hasReasoning = true;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
            }
            else if (event.getType() == RunEventType.MODEL_RESPONSE_DELTA)
            {
                String delta = (String) event.getAttributes().get("delta");
                if (delta != null && !delta.isEmpty())
                {
                    if (!hasAnswer && hasReasoning)
                    {
                        System.out.println("\n\n=== Answer ===");
                        hasAnswer = true;
                    }
                    System.out.print(delta);
                    System.out.flush();
                }
            }
        }
    }
}
