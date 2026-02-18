package stark.dataworks.coderaider.handoff;

public interface HandoffFilter {
    boolean allow(Handoff handoff);
}
