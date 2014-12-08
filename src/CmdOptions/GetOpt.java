package CmdOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetOpt {

    private final Map<Character, Option> indexByShortName;
    private final Map<String, Option> indexByName;

    public GetOpt(List<Option> options) throws Exception {
        this.indexByShortName = new LinkedHashMap<>();
        this.indexByName = new LinkedHashMap<>();
        for (Option opt : options) {
            if (this.indexByShortName.containsKey(opt.shortName)) {
                throw new Exception("Options have equals short name!");
            }
            this.indexByShortName.put(opt.shortName, opt);
            this.indexByName.put(opt.name, opt);
        }
    }

    private Option parseArg(String arg) {
        return null;
    }

    public Map<String, Option> mapOptions(String[] args) throws Exception {
        Map<String, Option> result = new LinkedHashMap<>();
        for (Option opt : this.listOptions(args)) {
            result.put(opt.name, opt);
        }
        return result;
    }

    public List<Option> listOptions(String[] args) throws Exception {
        List<Option> result = new ArrayList<>();
        Option lastOption = null;
        int element = 0;
        for (String arg : args) {
            boolean bThisIsArgument = true;
            boolean wasDigit = false;
            boolean bIsLongOptionName = false;
            boolean bEndArguments = false;
            for (int i = 0; i < arg.length(); i++) {
                char c = arg.charAt(i);
                if (i == 0 && c == '-') {
                    bThisIsArgument = false;
                    continue;
                }
                if (i == 1 && c == '-') {
                    if (arg.length() == 2) {
                        bEndArguments = true;
                        break;
                    }
                    bThisIsArgument = false;
                    bIsLongOptionName = true;
                    continue;
                }
                if (Character.isDigit(c)) {
                    if (bIsLongOptionName) {
                        if (wasDigit) {
                            throw new Exception("More 1 digits in long option name!");
                        } else {
                            wasDigit = true;
                            continue;
                        }
                    } else {
                        throw new Exception("Short option name can't be a digit!");
                    }
                }
                if (Character.isLetter(c)) {
                    if (!bIsLongOptionName && !bThisIsArgument) {
                        Option option = this.indexByShortName.get(c);
                        if (option == null) {
                            throw new Exception("Unknow option! Option: " + c + " Element: " + element);
                        }
                        result.add(lastOption = option);
                    }
                }
            }
            if (bEndArguments) {
                break;
            }
            if (bThisIsArgument && lastOption != null) {
                lastOption.addArgument(arg);
            }
            if (bIsLongOptionName) {
                Option option = this.indexByName.get(arg.substring(2));
                if (option == null) {
                    throw new Exception("Unknow option!");
                }
                result.add(lastOption = option);
            }
            element++;
        }
        if (lastOption != null && lastOption.hasArguments) {
            throw new Exception("Missing an argument!");
        }
        return result;
    }
}
