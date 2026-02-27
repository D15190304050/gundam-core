package stark.dataworks.coderaider.gundam.core.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for applying V4A diffs against text inputs.
 * This parser understands both the create-file syntax (only "+" prefixed lines)
 * and the default update syntax that includes context hunks.
 */
public final class DiffApplier
{
    private static final String END_PATCH = "*** End Patch";
    private static final String END_FILE = "*** End of File";
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r?\n");

    private DiffApplier()
    {
    }

    public enum ApplyDiffMode
    {
        DEFAULT,
        CREATE
    }

    public static String applyDiff(String input, String diff)
    {
        return applyDiff(input, diff, ApplyDiffMode.DEFAULT);
    }

    public static String applyDiff(String input, String diff, ApplyDiffMode mode)
    {
        if (diff == null || diff.isEmpty())
        {
            return input;
        }

        String newline = detectNewline(input, diff, mode);
        List<String> diffLines = normalizeDiffLines(diff);

        if (mode == ApplyDiffMode.CREATE)
        {
            return parseCreateDiff(diffLines, newline);
        }

        String normalizedInput = normalizeTextNewlines(input != null ? input : "");
        ParsedUpdateDiff parsed = parseUpdateDiff(diffLines, normalizedInput);
        return applyChunks(normalizedInput, parsed.getChunks(), newline);
    }

    private static String detectNewline(String input, String diff, ApplyDiffMode mode)
    {
        if (mode != ApplyDiffMode.CREATE && input != null && input.contains("\n"))
        {
            return input.contains("\r\n") ? "\r\n" : "\n";
        }
        return diff != null && diff.contains("\r\n") ? "\r\n" : "\n";
    }

    private static List<String> normalizeDiffLines(String diff)
    {
        List<String> lines = new ArrayList<>();
        Matcher m = NEWLINE_PATTERN.matcher(diff);
        int lastEnd = 0;
        while (m.find())
        {
            lines.add(diff.substring(lastEnd, m.start()));
            lastEnd = m.end();
        }
        if (lastEnd < diff.length())
        {
            lines.add(diff.substring(lastEnd));
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty())
        {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    private static String normalizeTextNewlines(String text)
    {
        return text.replace("\r\n", "\n");
    }

    private static String parseCreateDiff(List<String> lines, String newline)
    {
        List<String> output = new ArrayList<>();
        boolean inHunk = false;
        boolean hasHeaders = false;

        for (String line : lines)
        {
            if (line.startsWith("diff --git") || line.startsWith("new file mode") ||
                line.startsWith("index ") || line.startsWith("--- ") || line.startsWith("+++ "))
            {
                hasHeaders = true;
                continue;
            }

            if (line.startsWith("@@"))
            {
                inHunk = true;
                continue;
            }

            if (line.equals(END_PATCH) || line.startsWith("*** "))
            {
                break;
            }

            if (hasHeaders)
            {
                if (inHunk && line.startsWith("+"))
                {
                    output.add(line.substring(1));
                }
            }
            else
            {
                if (line.startsWith("+"))
                {
                    output.add(line.substring(1));
                }
            }
        }
        return String.join(newline, output);
    }

    private static ParsedUpdateDiff parseUpdateDiff(List<String> lines, String input)
    {
        List<String> extendedLines = new ArrayList<>(lines);
        extendedLines.add(END_PATCH);

        String[] inputLines = input.split("\n", -1);
        List<Chunk> chunks = new ArrayList<>();
        int cursor = 0;
        int index = 0;
        int fuzz = 0;

        while (index < extendedLines.size())
        {
            String line = extendedLines.get(index);

            if (line.equals(END_PATCH) || line.equals(END_FILE) ||
                line.startsWith("*** Update File:") || line.startsWith("*** Delete File:") ||
                line.startsWith("*** Add File:"))
            {
                break;
            }

            if (line.startsWith("@@"))
            {
                index++;
                continue;
            }

            ReadSectionResult section = readSection(extendedLines, index);
            if (section.getContext().isEmpty() && section.getChunks().isEmpty())
            {
                break;
            }

            ContextMatch findResult = findContext(inputLines, section.getContext().toArray(new String[0]), cursor, section.isEof());
            if (findResult.getNewIndex() == -1)
            {
                throw new IllegalArgumentException("Invalid Context at position " + cursor);
            }

            cursor = findResult.getNewIndex() + section.getContext().size();
            fuzz += findResult.getFuzz();
            index = section.getEndIndex();

            for (Chunk ch : section.getChunks())
            {
                chunks.add(new Chunk(
                    ch.getOrigIndex() + findResult.getNewIndex(),
                    ch.getDelLines(),
                    ch.getInsLines()
                ));
            }
        }

        return new ParsedUpdateDiff(chunks, fuzz);
    }

    private static ReadSectionResult readSection(List<String> lines, int startIndex)
    {
        List<String> context = new ArrayList<>();
        List<String> delLines = new ArrayList<>();
        List<String> insLines = new ArrayList<>();
        List<Chunk> sectionChunks = new ArrayList<>();
        int index = startIndex;
        String lastMode = "keep";

        while (index < lines.size())
        {
            String raw = lines.get(index);

            if (raw.startsWith("@@") || raw.startsWith(END_PATCH) ||
                raw.startsWith("*** Update File:") || raw.startsWith("*** Delete File:") ||
                raw.startsWith("*** Add File:") || raw.equals(END_FILE) || raw.equals("***"))
            {
                break;
            }

            index++;

            if (raw.isEmpty())
            {
                raw = " ";
            }

            char prefix = raw.charAt(0);
            String mode;
            String lineContent = raw.length() > 1 ? raw.substring(1) : "";

            if (prefix == '+')
            {
                mode = "add";
            }
            else if (prefix == '-')
            {
                mode = "delete";
            }
            else if (prefix == ' ')
            {
                mode = "keep";
            }
            else
            {
                throw new IllegalArgumentException("Invalid Line: " + raw);
            }

            boolean switchingToContext = "keep".equals(mode) && !"keep".equals(lastMode);
            if (switchingToContext && (!delLines.isEmpty() || !insLines.isEmpty()))
            {
                sectionChunks.add(new Chunk(context.size() - delLines.size(), new ArrayList<>(delLines), new ArrayList<>(insLines)));
                delLines.clear();
                insLines.clear();
            }

            if ("delete".equals(mode))
            {
                delLines.add(lineContent);
                context.add(lineContent);
            }
            else if ("add".equals(mode))
            {
                insLines.add(lineContent);
            }
            else
            {
                context.add(lineContent);
            }

            lastMode = mode;
        }

        if (!delLines.isEmpty() || !insLines.isEmpty())
        {
            sectionChunks.add(new Chunk(context.size() - delLines.size(), new ArrayList<>(delLines), new ArrayList<>(insLines)));
        }

        boolean eof = index < lines.size() && lines.get(index).equals(END_FILE);
        if (eof)
        {
            index++;
        }

        return new ReadSectionResult(context, sectionChunks, index, eof);
    }

    private static ContextMatch findContext(String[] lines, String[] context, int start, boolean eof)
    {
        if (context.length == 0)
        {
            return new ContextMatch(start, 0);
        }

        if (eof)
        {
            int endStart = Math.max(0, lines.length - context.length);
            ContextMatch endMatch = findContextCore(lines, context, endStart);
            if (endMatch.getNewIndex() != -1)
            {
                return endMatch;
            }
            ContextMatch fallback = findContextCore(lines, context, start);
            return new ContextMatch(fallback.getNewIndex(), fallback.getFuzz() + 10000);
        }

        return findContextCore(lines, context, start);
    }

    private static ContextMatch findContextCore(String[] lines, String[] context, int start)
    {
        for (int i = start; i <= lines.length - context.length; i++)
        {
            if (equalsSlice(lines, context, i, false, false))
            {
                return new ContextMatch(i, 0);
            }
        }

        for (int i = start; i <= lines.length - context.length; i++)
        {
            if (equalsSlice(lines, context, i, true, false))
            {
                return new ContextMatch(i, 1);
            }
        }

        for (int i = start; i <= lines.length - context.length; i++)
        {
            if (equalsSlice(lines, context, i, false, true))
            {
                return new ContextMatch(i, 100);
            }
        }

        return new ContextMatch(-1, 0);
    }

    private static boolean equalsSlice(String[] source, String[] target, int start, boolean stripTrailing, boolean stripAll)
    {
        if (start + target.length > source.length)
        {
            return false;
        }

        for (int offset = 0; offset < target.length; offset++)
        {
            String sourceValue = source[start + offset];
            String targetValue = target[offset];

            if (stripAll)
            {
                sourceValue = sourceValue.strip();
                targetValue = targetValue.strip();
            }
            else if (stripTrailing)
            {
                sourceValue = sourceValue.stripTrailing();
                targetValue = targetValue.stripTrailing();
            }

            if (!sourceValue.equals(targetValue))
            {
                return false;
            }
        }
        return true;
    }

    private static String applyChunks(String input, List<Chunk> chunks, String newline)
    {
        String[] origLines = input.split("\n", -1);
        List<String> destLines = new ArrayList<>();
        int cursor = 0;

        for (Chunk chunk : chunks)
        {
            if (chunk.getOrigIndex() > origLines.length)
            {
                throw new IllegalArgumentException("applyDiff: chunk.origIndex " + chunk.getOrigIndex() + " > input length " + origLines.length);
            }
            if (cursor > chunk.getOrigIndex())
            {
                throw new IllegalArgumentException("applyDiff: overlapping chunk at " + chunk.getOrigIndex() + " (cursor " + cursor + ")");
            }

            for (int i = cursor; i < chunk.getOrigIndex(); i++)
            {
                destLines.add(origLines[i]);
            }
            cursor = chunk.getOrigIndex();

            destLines.addAll(chunk.getInsLines());
            cursor += chunk.getDelLines().size();
        }

        for (int i = cursor; i < origLines.length; i++)
        {
            destLines.add(origLines[i]);
        }

        return String.join(newline, destLines);
    }

    private static class Chunk
    {
        private final int origIndex;
        private final List<String> delLines;
        private final List<String> insLines;

        public Chunk(int origIndex, List<String> delLines, List<String> insLines)
        {
            this.origIndex = origIndex;
            this.delLines = delLines;
            this.insLines = insLines;
        }

        public int getOrigIndex()
        {
            return origIndex;
        }

        public List<String> getDelLines()
        {
            return delLines;
        }

        public List<String> getInsLines()
        {
            return insLines;
        }
    }

    private static class ParsedUpdateDiff
    {
        private final List<Chunk> chunks;
        private final int fuzz;

        public ParsedUpdateDiff(List<Chunk> chunks, int fuzz)
        {
            this.chunks = chunks;
            this.fuzz = fuzz;
        }

        public List<Chunk> getChunks()
        {
            return chunks;
        }

        public int getFuzz()
        {
            return fuzz;
        }
    }

    private static class ReadSectionResult
    {
        private final List<String> context;
        private final List<Chunk> chunks;
        private final int endIndex;
        private final boolean eof;

        public ReadSectionResult(List<String> context, List<Chunk> chunks, int endIndex, boolean eof)
        {
            this.context = context;
            this.chunks = chunks;
            this.endIndex = endIndex;
            this.eof = eof;
        }

        public List<String> getContext()
        {
            return context;
        }

        public List<Chunk> getChunks()
        {
            return chunks;
        }

        public int getEndIndex()
        {
            return endIndex;
        }

        public boolean isEof()
        {
            return eof;
        }
    }

    private static class ContextMatch
    {
        private final int newIndex;
        private final int fuzz;

        public ContextMatch(int newIndex, int fuzz)
        {
            this.newIndex = newIndex;
            this.fuzz = fuzz;
        }

        public int getNewIndex()
        {
            return newIndex;
        }

        public int getFuzz()
        {
            return fuzz;
        }
    }
}
