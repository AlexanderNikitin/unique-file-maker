package CommandLineArguments;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandLineArgumentParseResult {

    private final String[] operands;
    private final Option[] options;
    private Map<String, Option> map;

    public String[] getOperands() {
        return this.operands;
    }

    public Option[] getOptions() {
        return this.options;
    }

    public CommandLineArgumentParseResult(Option[] options, String[] operands) {
        this.operands = operands;
        this.options = options;
    }

    public Map<String, Option> mapOptions() throws Exception {
        if (this.map == null) {
            this.map = new LinkedHashMap<>();
            for (Option opt : this.options) {
                this.map.put(opt.name, opt);
            }
        }
        return this.map;
    }
}
