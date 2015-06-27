package DuplicateFinder;

import CommandLineArguments.*;
import FileComparer.FileComparer;
import FileSearcher.FileSearcher;
import FileDeleter.CheckedFile;
import FileDeleter.DuplicateDeleteSoluter;
import FileDeleter.Rule;
import FileDeleter.FileDeleter;
import FileDeleter.Param;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DuplicateFinder {

    private static String str_repeat_optimal(String s, int c) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty() || c <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(c * s.length());
        for (int i = 0; i < c; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static void print(Map<Long, List<File[]>> dupsGroups) {
        dupsGroups.entrySet().stream().forEach(arFileOneSize -> {
            System.out.println(arFileOneSize.getKey());
            System.out.println(str_repeat_optimal("-", 50));
            arFileOneSize.getValue().stream().filter(dups -> dups.length > 1).forEach(dups -> {
                for (File dup : dups) {
                    System.out.println(dup.toString());
                }
                System.out.println(str_repeat_optimal("-", 50));
            });
        });
    }

    private static String getHumanBytes(long speedByte) {
        long byteInKb = 1024;
        long byteInMb = byteInKb * byteInKb;
        long byteInGb = byteInMb * byteInKb;
        if (speedByte >= byteInGb) {
            return (speedByte / byteInGb) + " GB";
        }
        if (speedByte >= byteInMb) {
            return (speedByte / byteInMb) + " MB";
        }
        if (speedByte >= byteInKb) {
            return (speedByte / byteInKb) + " KB";
        }
        return speedByte + " B";
    }

    public static void main(String[] args) throws Exception {
        List<Option> lOptions = new ArrayList<>();
        lOptions.add(new Option("ext", true));
        lOptions.add(new Option("path", true));
        lOptions.add(new Option("recursive"));
        lOptions.add(new Option("groupbyext"));
        lOptions.add(new Option("saveonlyone", 'o'));
        lOptions.add(new Option("check"));
        lOptions.add(new Option("delete"));
        lOptions.add(new Option("help"));
        lOptions.add(new Option("exclude", 'x', true));
        lOptions.add(new Option("saveprefer", true));
        lOptions.add(new Option("log", true));
        lOptions.add(new Option("nodelete", true));

        Map<String, Option> opts = new CommandLineArgumentParser(lOptions, true).parse(args).mapOptions();

        if (opts.containsKey("help")) {
            String help = ""
                    + "--ext, -e <extension[ extension2]>\n"
                    + "\tOptional. File extension(s) to find.\n"
                    + "--path, -p <directory[ directory2]>\n"
                    + "\tOptional. Directories where find files.\n"
                    + "--recursive, -r\n"
                    + "\tFind files in subdirectories.\n"
                    + "--groupbyext, -g\n"
                    + "\tEquals files, which has not equals extensions, are not duplicates.\n"
                    + "--saveonlyone, -o\n"
                    + "\tIf some rules save more than one file in duplicates group, program realy save only random first from saved by reles.\n"
                    + "--check, -c\n"
                    + "\tCheck duplicated equality by MD5 algorithm.\n"
                    + "--delete, -d\n"
                    + "\tIf this option missing program will not delete files.\n"
                    + "--exclude, -x\n"
                    + "\tOptional. Regular expression to exclude directories to scan.\n"
                    + "--saveprefer, -s <directory1[ directory2]>\n"
                    + "\tOptional. Priority list of directories for save one of all duplicates\n"
                    + "--log, -l <path to log[ path to log 2]>\n"
                    + "\tOptional. Path to log file for logging file relations.\n"
                    + "--nodelete, -n\n"
                    + "\tOptional. List of directories wich protect child files of deleting.\n"
                    + "--help, -h\n"
                    + "\tIf you read this annotatin, you know it:)\n";
            System.out.println(help);
            return;
        }

        //log
        Option oLog = opts.get("log");
        Log log = new Log(oLog == null ? null : oLog.getArguments());

        for (Option opt : opts.values()) {
            log.log(opt.name);
            if (opt.hasArguments) {
                for (String s : opt.getArguments()) {
                    log.log("\t" + s);
                }
            }
        }

        //ext init
        List<String> exts = opts.get("ext") == null ? null : opts.get("ext").getArguments();

        //start dirs init
        Option path = opts.get("path");
        List<String> allDirs = path == null ? null : path.getArguments();
        String[] aStartDirs;
        if (allDirs == null) {
            aStartDirs = new String[]{new File("").getCanonicalPath()};
        } else {
            aStartDirs = allDirs.toArray(new String[allDirs.size()]);
        }

        //recursive init
        boolean bRecursive = opts.containsKey("recursive");

        //exclude
        Option optExclude = opts.get("exclude");
        List<String> lsExclide = null;
        if (optExclude != null) {
            lsExclide = optExclude.getArguments();
        }

        List<File> files = new FileSearcher(exts, bRecursive, lsExclide).search(aStartDirs);

        //diff by extension init
        boolean bDiffByExt = opts.containsKey("groupbyext");

        //need check
        boolean bNeedCheck = opts.containsKey("check");

        //save only one
        boolean bSaveOnlyOne = opts.containsKey("saveonlyone");

        //save directory priority (prefer save in dirictory)
        List<String> lSavePrefer = null;
        Option oSavePrefer = opts.get("saveprefer");
        if (oSavePrefer != null) {
            lSavePrefer = oSavePrefer.getArguments();
        }

        //no delete list
        List<String> lNoDelete = null;
        Option oNoDelete = opts.get("nodelete");
        if (oNoDelete != null) {
            lNoDelete = oNoDelete.getArguments();
        }

        //need check
        boolean bDel = opts.containsKey("delete");

        System.out.println("All found files: " + files.size());

        FileComparer fileComparer = new FileComparer(bDiffByExt, bNeedCheck);

        if (fileComparer.compare(files)) {
            Map<Long, List<File[]>> compare = fileComparer.getResult();
            //print(compare);

            List<Rule> lSaveRules = new ArrayList<>();

            lSaveRules.add(new Rule(Param.IS_ENGLISH_FILE_NAME, false));
            lSaveRules.add(new Rule(Param.IS_COPY, false));
            lSaveRules.add(new Rule(Param.DIRECTORY_DEPTH, true));
            lSaveRules.add(new Rule(Param.FILENAME_LENGTH, true));
            lSaveRules.add(new Rule(Param.PATH_LENGTH, true));

            DuplicateDeleteSoluter dds = new DuplicateDeleteSoluter(lSaveRules, bSaveOnlyOne, lSavePrefer, lNoDelete);

            List<CheckedFile[]> deletePreparation = new ArrayList<>();
            compare.values().stream().forEach(groups -> {
                groups.stream()
                        .filter(grup -> grup.length > 1)
                        .map(group -> dds.sol(group))
                        .filter(group -> group != null)
                        .forEach(group -> deletePreparation.add(group));
            });
            long delSize = 0;
            for (CheckedFile[] group : deletePreparation) {
                for (CheckedFile cf : group) {
                    log.log((cf.del ? "[x] " : "[ ] ") + cf.cacheAbsolutePath);
                    if (cf.del) {
                        delSize += cf.file.length();
                    }
                }
                log.log(str_repeat_optimal("-", 100));
            }
            log.log("Delete size: " + getHumanBytes(delSize));
            if (bDel) {
                FileDeleter fd = new FileDeleter();
                int cnt = 0;
                for (CheckedFile[] group : deletePreparation) {
                    boolean bIsNotDel = false;
                    for (CheckedFile file : group) {
                        if (!file.del) {
                            bIsNotDel = true;
                            break;
                        }
                    }
                    if (!bIsNotDel) {
                        throw new Exception("All files will die!!!");
                    }
                    for (CheckedFile f : group) {
                        if (f.del) {
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
    }
}
