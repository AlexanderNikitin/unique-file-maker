package finder;

import comparing.FileComparator;
import deleting.*;
import fs.FileSearcher;
import picocli.CommandLine;
import utils.Log;
import utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CommandLine.Command(name = "df")
public class DuplicateFinder implements Runnable {
    @CommandLine.Option(names = {"-p", "--paths"}, arity = "1..*")
    String[] paths = null;

    @CommandLine.Option(names = {"-e", "--exts"}, arity = "0..*")
    String[] exts = null;

    @CommandLine.Option(names = {"-r", "--recursive"})
    boolean recursive = false;

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

    @CommandLine.Option(names = {"-l, --logs"}, arity = "0..*")
    String[] logs = null;

    @CommandLine.Option(names = {"-n, --nodelete"}, arity = "0..*")
    String[] noDelete = null;

    static void main(String[] args) {
        System.exit(new CommandLine(new DuplicateFinder()).execute(args));
    }

    @Override
    public void run() {
        Log log = new Log(logs);

        try {
            List<File> files = new FileSearcher(exts, recursive, excludes).search(paths);


            System.out.println("All found files: " + files.size());

            FileComparator fileComparator = new FileComparator(groupbyext, check);

            if (fileComparator.compare(files)) {
                Map<Long, List<File[]>> compare = fileComparator.getResult();
                //print(compare);

                DuplicateDeleteSolver dds = getSolver();

                List<CheckedFile[]> deletePreparation = new ArrayList<>();
                compare.values().forEach(groups -> groups.stream()
                        .filter(group -> group.length > 1)
                        .map(dds::solve)
                        .filter(Objects::nonNull)
                        .forEach(deletePreparation::add));
                long delSize = 0;
                for (CheckedFile[] group : deletePreparation) {
                    for (CheckedFile cf : group) {
                        log.log((cf.delete ? "[x] " : "[ ] ") + cf.cacheAbsolutePath);
                        if (cf.delete) {
                            delSize += cf.file.length();
                        }
                    }
                    log.log(Utils.repeatString("-", 100));
                }
                log.log("Delete size: " + Utils.getHumanDataSize(delSize));
                if (delete) {
                    FileDeleter fd = new FileDeleter();
                    int cnt = 0;
                    for (CheckedFile[] group : deletePreparation) {
                        boolean bIsNotDel = false;
                        for (CheckedFile file : group) {
                            if (!file.delete) {
                                bIsNotDel = true;
                                break;
                            }
                        }
                        if (!bIsNotDel) {
                            throw new Exception("All files will die!!!");
                        }
                        for (CheckedFile f : group) {
                            if (f.delete) {
                                if (fd.delete(f.file)) {
                                    cnt++;
                                } else {
                                    System.out.println("Error delete: " + f.cacheAbsolutePath);
                                }
                            }
                        }
                    }
                    log.log("Deleted: " + cnt);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
