package FileDeleter;

public class Rule {

    public final Param param;
    public final boolean saveByMax;

    public Rule(Param param, boolean saveByMax) {
        this.param = param;
        this.saveByMax = saveByMax;
    }
}
