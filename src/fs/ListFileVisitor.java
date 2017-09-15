package fs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class ListFileVisitor extends SimpleFileVisitor<Path> {

    private static int FLAGS = Pattern.CASE_INSENSITIVE;

    private static Pattern[] SKIP_ALLWAYS = new Pattern[]{
            Pattern.compile("_files$", FLAGS),
            Pattern.compile(Pattern.quote("$RECYCLE.BIN"), FLAGS),
            Pattern.compile(Pattern.quote(File.separatorChar + "backup_") + "\\d+$", FLAGS)
    };

    final List<File> files = new ArrayList<>();
    private final FilenameFilter filenameFilter;
    private final Set<String> setVisitedDirs;
    private final List<Pattern> skipPatternDir;

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) {
        File file = path.toFile();
        String n = file.getAbsolutePath();
        for (Pattern p : this.skipPatternDir) {
            if (p.matcher(n).find()) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        try {
            String canonical = file.getCanonicalPath();
            if (this.setVisitedDirs.contains(canonical)) {
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                this.setVisitedDirs.add(canonical);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
        File file = path.toFile();
        if (file.length() > 0 && this.filenameFilter.accept(null, file.getName())) {
            this.files.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    public ListFileVisitor(FilenameFilter filenameFilter, List<String> exclude) {
        this.filenameFilter = filenameFilter;
        this.setVisitedDirs = new HashSet<>();

        this.skipPatternDir = new ArrayList<>();
        this.skipPatternDir.addAll(Arrays.asList(SKIP_ALLWAYS));

        if (exclude != null) {
            for (String s : exclude) {
                try {
                    this.skipPatternDir.add(Pattern.compile(s, FLAGS));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
