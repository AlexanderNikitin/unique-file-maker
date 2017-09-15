package cl;

import java.util.*;

public class CommandLineArgumentParser {

    private final Map<Character, Option> indexByShortName;
    private final Map<String, Option> indexByName;
    private final boolean escape;

    public CommandLineArgumentParser(List<Option> options, boolean escape) throws Exception {
        this.indexByShortName = new LinkedHashMap<>();
        this.indexByName = new LinkedHashMap<>();
        for (Option opt : options) {
            if (this.indexByShortName.containsKey(opt.shortName)) {
                throw new Exception("Options have equals short name!");
            }
            this.indexByShortName.put(opt.shortName, opt);
            this.indexByName.put(opt.name, opt);
        }
        this.escape = escape;
    }

    public CommandLineArgumentParseResult parse(String[] args) throws Exception {
        if (args == null) {
            throw new Exception("Argument is NULL!");
        }
        List<Option> options = new ArrayList<>();
        Option lastOption = null;
        int nElement = 0;
        for (String arg : args) {
            boolean bThisIsArgument = true;
            boolean bWasDigit = false;
            boolean bIsLongOptionName = false;
            boolean bEndOptions = false;
            boolean bEscapedArgument = false;
            for (int i = 0; i < arg.length(); i++) {
                char c = arg.charAt(i);
                if (i == 0) {
                    if (c == '-') {
                        if (lastOption != null && lastOption.needAddArgument()) {
                            throw new Exception("Must be an argument! Element number: " + nElement + " Element: " + arg);
                        }
                        bThisIsArgument = false;
                        continue;
                    }
                    if (this.escape && c == '\\') {
                        bEscapedArgument = true;
                        continue;
                    }
                }
                if (bThisIsArgument) {
                    if (lastOption == null) {
                        throw new Exception("Option must be first!");
                    }
                    break;
                } else {
                    if (i == 1 && c == '-') {
                        if (arg.length() == 2) {
                            bEndOptions = true;
                            break;
                        }
                        bIsLongOptionName = true;
                        continue;
                    }
                    if (Character.isDigit(c)) {
                        if (bIsLongOptionName) {
                            if (bWasDigit) {
                                if (i == 2) {
                                    throw new Exception("Option name must begin from letter!");
                                } else {
                                    throw new Exception("More 1 digits in long option name!");
                                }
                            } else {
                                bWasDigit = true;
                                continue;
                            }
                        } else {
                            throw new Exception("Short option name can't be a digit!");
                        }
                    }
                    if (Character.isLetter(c)) {
                        if (!bIsLongOptionName) {
                            Option option = this.indexByShortName.get(c);
                            if (option == null) {
                                throw new Exception("Unknow option! Option: " + c + " Element number: " + nElement + " Element: " + arg);
                            }
                            options.add(lastOption = option);
                        }
                        continue;
                    }
                    throw new Exception("Invalid character!");
                }
            }
            nElement++;
            if (bEndOptions) {
                break;
            }
            if (bThisIsArgument && lastOption != null) {
                if (bEscapedArgument) {
                    lastOption.addArgument(arg.substring(1));
                } else {
                    lastOption.addArgument(arg);
                }
            }
            if (bIsLongOptionName) {
                String sOptName = arg.substring(2);
                Option option = this.indexByName.get(sOptName);
                if (option == null) {
                    throw new Exception("Unknow option: " + sOptName + " Element number: " + nElement + " Element: " + arg);
                }
                options.add(lastOption = option);
            }
        }
        if (lastOption != null && lastOption.needAddArgument()) {
            throw new Exception("Missing an argument!");
        }
        return new CommandLineArgumentParseResult(options.toArray(new Option[options.size()]), Arrays.copyOfRange(args, nElement, args.length));
    }
}
