package CmdOptions;

import java.util.ArrayList;
import java.util.List;

public class Option {

    public final String name;
    public final char shortName;
    public final boolean hasArguments;
    private final List<String> args;

    public Option(String name, boolean hasArgument) throws Exception {
        this(name, name == null || name.isEmpty() ? 0 : name.charAt(0), hasArgument);
    }

    public Option(String name) throws Exception {
        this(name, name == null || name.isEmpty() ? 0 : name.charAt(0));
    }

    public Option(String name, char shortName) throws Exception {
        this(name, shortName, false);
    }

    public Option(String name, char shortName, boolean hasArgument) throws Exception {
        if (name == null || name.isEmpty()) {
            throw new Exception("Empty option name!");
        }
        if (!Character.isLetter(shortName)) {
            throw new Exception("Invalid short option name!");
        }
        this.name = name;
        this.shortName = shortName;
        this.hasArguments = hasArgument;
        if (this.hasArguments) {
            this.args = new ArrayList<>();
        } else {
            this.args = null;
        }
    }

    public List<String> getArguments() throws Exception {
        if (this.hasArguments) {
            return this.args;
        } else {
            throw new Exception("This option hasn't arguments! Can't get!");
        }
    }

    public void addArgument(String arg) throws Exception {
        if (this.hasArguments && this.args != null) {
            this.args.add(arg);
        } else {
            throw new Exception("This option hasn't arguments! Can't set!");
        }
    }
}
