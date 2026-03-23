package stark.dataworks.coderaider.genericagent.core.excalibur;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Task request metadata modeled after trae-agent task bootstrap inputs.
 */
public final class ExcaliburTaskRequest
{
    private final String task;
    private final Path projectPath;
    private final String issue;
    private final String baseCommit;
    private final boolean mustPatch;
    private final Path patchPath;

    private ExcaliburTaskRequest(Builder builder)
    {
        this.task = Objects.requireNonNull(builder.task, "task");
        this.projectPath = Objects.requireNonNull(builder.projectPath, "projectPath").toAbsolutePath().normalize();
        this.issue = builder.issue == null ? "" : builder.issue;
        this.baseCommit = builder.baseCommit;
        this.mustPatch = builder.mustPatch;
        this.patchPath = builder.patchPath == null ? null : builder.patchPath.toAbsolutePath().normalize();
    }

    public String getTask()
    {
        return task;
    }

    public Path getProjectPath()
    {
        return projectPath;
    }

    public String getIssue()
    {
        return issue;
    }

    public String getBaseCommit()
    {
        return baseCommit;
    }

    public boolean isMustPatch()
    {
        return mustPatch;
    }

    public Path getPatchPath()
    {
        return patchPath;
    }

    public static Builder builder(String task, Path projectPath)
    {
        return new Builder(task, projectPath);
    }

    public static final class Builder
    {
        private final String task;
        private final Path projectPath;
        private String issue;
        private String baseCommit;
        private boolean mustPatch;
        private Path patchPath;

        private Builder(String task, Path projectPath)
        {
            this.task = task;
            this.projectPath = projectPath;
        }

        public Builder issue(String issue)
        {
            this.issue = issue;
            return this;
        }

        public Builder baseCommit(String baseCommit)
        {
            this.baseCommit = baseCommit;
            return this;
        }

        public Builder mustPatch(boolean mustPatch)
        {
            this.mustPatch = mustPatch;
            return this;
        }

        public Builder patchPath(Path patchPath)
        {
            this.patchPath = patchPath;
            return this;
        }

        public ExcaliburTaskRequest build()
        {
            return new ExcaliburTaskRequest(this);
        }
    }
}
