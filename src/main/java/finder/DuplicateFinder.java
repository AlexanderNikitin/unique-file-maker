package finder;

import command.ScanCommand;
import deleting.DuplicateDeleteSolver;
import deleting.Parameters;
import deleting.Rule;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "df", subcommands = {ScanCommand.class})
public class DuplicateFinder implements Runnable {

    @CommandLine.Option(names = {"-e", "--exts"}, arity = "0..*")
    String[] exts = null;

    @CommandLine.Option(names = {"-g", "--groupbyext"})
    boolean groupbyext = false;

    @CommandLine.Option(names = {"-o", "--saveonlyone"})
    boolean saveOnlyOne = false;

    @CommandLine.Option(names = {"-c", "--check"})
    boolean check = false;

    @CommandLine.Option(names = {"-d", "--delete"})
    boolean delete = false;

    @CommandLine.Option(names = {"-x", "--excludes"}, arity = "0..*")
    String[] excludes = null;

    @CommandLine.Option(names = {"-s", "--savepreferes"}, arity = "0..*")
    String[] savePreferes = null;

    @CommandLine.Option(names = {"-n, --nodelete"}, arity = "0..*")
    String[] noDelete = null;

    static void main(String[] args) {
        System.exit(new CommandLine(new DuplicateFinder()).execute(args));
    }

    @Override
    public void run() {

        //TODO
    }

    private DuplicateDeleteSolver getSolver() {
        List<Rule> lSaveRules = new ArrayList<>();

        lSaveRules.add(new Rule(Parameters.IS_ENGLISH_FILE_NAME, false));
        lSaveRules.add(new Rule(Parameters.IS_COPY, false));
        lSaveRules.add(new Rule(Parameters.DIRECTORY_DEPTH, true));
        lSaveRules.add(new Rule(Parameters.FILENAME_LENGTH, true));
        lSaveRules.add(new Rule(Parameters.PATH_LENGTH, true));

        return new DuplicateDeleteSolver(lSaveRules, saveOnlyOne, savePreferes, noDelete);
    }
}
