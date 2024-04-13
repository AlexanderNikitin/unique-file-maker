package cl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandLineArgumentParseResult {
    private final Option[] options;
    private Map<String, Option> map;

    public CommandLineArgumentParseResult(Option[] options) {
        this.options = options;
    }

    public Map<String, Option> mapOptions() {
        if (this.map == null) {
            this.map = new LinkedHashMap<>();
            for (Option opt : this.options) {
                this.map.put(opt.name, opt);
            }
        }
        return this.map;
    }
}
