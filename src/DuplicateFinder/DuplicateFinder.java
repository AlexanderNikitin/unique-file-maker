package DuplicateFinder;

import CommandLineArguments.*;
import FileDeleter.CheckedFile;
import FileDeleter.DuplicateDeleteSoluter;
import FileDeleter.Rule;
import FileDeleter.FileDeleter;
import FileDeleter.Param;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DuplicateFinder {

    public final Map<Long, List<File[]>> dupsGroups = new HashMap<>();

    private Map<Long, List<File>> group(List<File> files) {
        System.out.println("Grouping...");
        Map<Long, List<File>> hmSizeGrouping = new HashMap<>();
        for (File f : files) {
            long size = f.length();
            if (size > 0) {
                List<File> lEqualSize;
                if (hmSizeGrouping.containsKey(size)) {
                    lEqualSize = hmSizeGrouping.get(size);
                } else {
                    hmSizeGrouping.put(size, lEqualSize = new ArrayList<>());
                }
                lEqualSize.add(f);
            }
        }
        return hmSizeGrouping;
    }

    public String getFileExtensionByName(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i + 1) : "";
    }

    private void compare(Map<Long, List<File>> hmSizeGrouping, boolean diffByExt) throws IOException {
        System.out.println("Compare...");
        long t = System.currentTimeMillis();
        for (Map.Entry<Long, List<File>> eOneSize : hmSizeGrouping.entrySet()) {
            List<File> lOneSize = eOneSize.getValue();
            if (lOneSize.size() > 1) {
                if (diffByExt) {
                    Map<String, List<File>> hmGroupedByExt = new HashMap<>();
                    lOneSize.stream().forEach(file -> {
                        String ext = getFileExtensionByName(file.getName());
                        List<File> filesByExt;
                        if (hmGroupedByExt.containsKey(ext)) {
                            filesByExt = hmGroupedByExt.get(ext);
                        } else {
                            hmGroupedByExt.put(ext, filesByExt = new ArrayList<>());
                        }
                        filesByExt.add(file);
                    });
                    List<File[]> groups = new ArrayList<>();
                    for (List<File> filesByExt : hmGroupedByExt.values()) {
                        if (filesByExt.size() > 1) {
                            groups.addAll(new FileSizeGroup(filesByExt).getGroups());
                        }
                    }
                    this.dupsGroups.put(eOneSize.getKey(), groups);
                } else {
                    this.dupsGroups.put(eOneSize.getKey(), new FileSizeGroup(lOneSize).getGroups());
                }
            }
        }
        System.out.println("Compare time: " + (System.currentTimeMillis() - t));
    }

    public DuplicateFinder(List<File> files) throws Exception {
        this(files, false);
    }

    public DuplicateFinder(List<File> files, boolean diffByExt) throws Exception {
        this.compare(this.group(files), diffByExt);
    }

    public void print() {
        this.dupsGroups.entrySet().stream().forEach(arFileOneSize -> {
            System.out.println(arFileOneSize.getKey());
            System.out.println(str_repeat("-", 50));
            arFileOneSize.getValue().stream().filter(dups -> dups.length > 1).forEach(dups -> {
                for (File dup : dups) {
                    System.out.println(dup.toString());
                }
                System.out.println(str_repeat("-", 50));
            });
        });
    }

    private static String getHumanSpeed(long speedByte) {
        long byteInKb = 1024;
        long byteInMb = byteInKb * byteInKb;
        long byteInGb = byteInMb * byteInKb;
        if (speedByte >= byteInGb) {
            return (speedByte / byteInGb) + " GB/s";
        }
        if (speedByte >= byteInMb) {
            return (speedByte / byteInMb) + " MB/s";
        }
        if (speedByte >= byteInKb) {
            return (speedByte / byteInKb) + " KB/s";
        }
        return speedByte + " B/s";
    }

    private boolean check() throws NoSuchAlgorithmException {
        System.out.println("Checking");
        long allSize = 0;
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.dupsGroups.entrySet()) {
            List<File[]> dupGruops = arFileOneSize.getValue();
            long groupSize = arFileOneSize.getKey();
            for (File[] dups : dupGruops) {
                long n = dups.length;
                if (n > 1) {
                    long delta = groupSize * n;
                    assert (Long.MAX_VALUE - delta) > allSize : delta + " " + allSize;
                    allSize += delta;
                }
            }
        }
        System.out.println("All size: " + allSize);

        //long t = System.currentTimeMillis();
        int errGrCnt = 0;
        long curSize = 0;
        long oldPercent = -1;
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.dupsGroups.entrySet()) {
            List<File[]> dupGruops = arFileOneSize.getValue();
            if (dupGruops.isEmpty()) {
                continue;
            }
            long groupSize = arFileOneSize.getKey();
            for (File[] dups : dupGruops) {
                int n = dups.length;
                if (n > 1) {
                    byte[][] d = new byte[n][];
                    for (int i = 0; i < n; i++) {
                        File dup = dups[i];
                        try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(dup.getPath())), md)) {
                            while (dis.read() != -1);
                        } catch (FileNotFoundException e) {
                            System.err.println(e.getMessage());
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        d[i] = md.digest();
                    }
                    boolean bErr = false;
                    for (int i = 1; i < d.length; i++) {
                        if (!Arrays.equals(d[0], d[i])) {
                            System.out.println(dups[i].toString());
                            bErr = true;
                        }
                    }
                    if (bErr) {
                        System.out.println("Default: " + dups[0].toString());
                        System.out.println();
                        errGrCnt++;
                    }

                    long delta = n * groupSize;
                    assert (Long.MAX_VALUE - delta) > curSize : delta + " " + allSize;
                    curSize += delta;

                    long percent = curSize * 100 / allSize;
                    if (percent > oldPercent) {
                        System.out.println(percent + "%");
                        oldPercent = percent;
                    }
                }
            }
            /*long curT = System.currentTimeMillis();
             long timeSec = (curT - t) / 1000;
             long speedByte = curSize;
             if (timeSec > 0) {
             speedByte /= timeSec;
             }
             out.println((speedByte) + " B/s");*/
        }
        System.out.println("All errord groups: " + errGrCnt);
        return errGrCnt == 0;
    }

    private static String str_repeat(String s, int c) {
        StringBuilder sb = new StringBuilder(s.length() * c);
        for (int i = 0; i < c; i++) {
            sb.append(s);
        }
        return sb.toString();
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

        Map<String, Option> opts = new CommandLineArgumentParser(lOptions, true).parse(args).mapOptions();

        boolean bHelp = opts.containsKey("help");
        if (bHelp) {
            String help = ""
                    + "--ext, -e <extension[ extension2]>\n"
                    + "\tOptional. File extension(s) to find.\n"
                    + "--path, -p <directory[ directory2]>\n"
                    + "\tDirectories where find files.\n"
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
                    + "--help, -h\n"
                    + "\tIf you read this annotatin, you know it:)\n";
            System.out.println(help);
            return;
        }

        for (Option opt : opts.values()) {
            System.out.println(opt.name);
            if (opt.hasArguments) {
                for (String s : opt.getArguments()) {
                    System.out.println("\t" + s);
                }
            }
        }

        //ext init
        List<String> exts = opts.get("ext") == null ? null : opts.get("ext").getArguments();
        FilenameFilter fnf = new ExtFileNameFilter(exts);

        //start dirs init
        List<String> allDirs = opts.get("path") == null ? null : opts.get("path").getArguments();
        String[] aStartDirs;
        if (allDirs == null) {
            aStartDirs = new String[]{new File("").getCanonicalPath()};
        } else {
            aStartDirs = allDirs.toArray(new String[allDirs.size()]);
        }

        //recursive init
        boolean bRecursive = opts.containsKey("recursive");

        //diff by extension init
        boolean bDiffByExt = opts.containsKey("groupbyext");

        //save only one
        boolean bSaveOnlyOne = opts.containsKey("saveonlyone");

        //need check
        boolean bNeedCheck = opts.containsKey("check");

        //need check
        boolean bDel = opts.containsKey("delete");

        //exclude
        List<String> lsExclide = null;
        Option optExclude = opts.get("exclude");
        if (optExclude != null) {
            lsExclide = optExclude.getArguments();
        }

        List<File> files;
        if (bRecursive) {
            ListFileVisitor lfv = new ListFileVisitor(fnf, lsExclide);
            Set<FileVisitOption> so = new HashSet<>();
            so.add(FileVisitOption.FOLLOW_LINKS);
            for (String startDir : aStartDirs) {
                System.out.println(startDir);
                try {
                    Files.walkFileTree(new File(startDir).toPath().toRealPath(LinkOption.NOFOLLOW_LINKS), so, Integer.MAX_VALUE, lfv);
                } catch (FileSystemException e) {
                    System.out.println("FileSystemException: " + e.getFile());
                }
            }
            files = lfv.files;
        } else {
            files = new ArrayList<>();
            for (String startDir : aStartDirs) {
                System.out.println(startDir);
                File[] foundFiles = new File(startDir).listFiles(fnf);
                for (File file : foundFiles) {
                    if (file.isFile()) {
                        files.add(file);
                    }
                }
            }
        }

        System.out.println("All found files: " + files.size());
        DuplicateFinder duplicateFinder = new DuplicateFinder(files, bDiffByExt);
        //duplicateFinder.print();

        if ((bNeedCheck && duplicateFinder.check()) || !bNeedCheck) {
            List<Rule> lRules = new ArrayList<>();

            lRules.add(new Rule(Param.IS_ENGLISH_FILE_NAME, false));
            lRules.add(new Rule(Param.IS_COPY, false));
            lRules.add(new Rule(Param.DIRECTORY_DEPTH, true));
            lRules.add(new Rule(Param.FILENAME_LENGTH, true));
            lRules.add(new Rule(Param.PATH_LENGTH, true));

            DuplicateDeleteSoluter dds = new DuplicateDeleteSoluter(lRules, bSaveOnlyOne);

            List<CheckedFile[]> deletePreparation = new ArrayList<>();
            duplicateFinder.dupsGroups.values().stream().forEach(groups -> {
                groups.stream().filter(grup -> grup.length > 1).forEach(group -> {
                    deletePreparation.add(dds.sol(group));
                });
            });
            for (CheckedFile[] group : deletePreparation) {
                for (CheckedFile cf : group) {
                    System.out.println((cf.del ? "[x] " : "[ ] ") + cf.file.getAbsolutePath());
                }
                System.out.println(str_repeat("-", 100));
            }
            if (bDel) {
                FileDeleter fd = new FileDeleter();
                for (CheckedFile[] group : deletePreparation) {
                    fd.delDups(group);
                }
                System.out.println("Deleted: " + fd.cnt());
            }
        }
    }
}
