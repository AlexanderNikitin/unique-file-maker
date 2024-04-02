package deleting;

public class Rule {

    public final Parameters parameters;
    public final boolean saveByMax;

    public Rule(Parameters parameters, boolean saveByMax) {
        this.parameters = parameters;
        this.saveByMax = saveByMax;
    }
}
