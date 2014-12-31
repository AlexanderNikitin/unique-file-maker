package FileSearcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

public class ListFileVisitor extends SimpleFileVisitor<Path> {

    public final List<File> files = new ArrayList<>();
    private final FilenameFilter fnf;
    private final Set<String> setVisitedDirs;
    private final List<Pattern> skipDir;

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attr) {
        File file = path.toFile();
        String n = file.getName();
        for (Pattern p : this.skipDir) {
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
    public FileVisitResult visitFile(Path path, BasicFileAttributes attr) {
        File file = path.toFile();
        if (file.length() > 0 && this.fnf.accept(null, file.getName())) {
            this.files.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    public ListFileVisitor(FilenameFilter fnf, List<String> lsExclide) {
        this.fnf = fnf;
        this.setVisitedDirs = new HashSet<>();

        this.skipDir = new ArrayList<>();
        int flags = Pattern.CASE_INSENSITIVE;
        this.skipDir.add(Pattern.compile("^backup_\\d+$", flags));
        this.skipDir.add(Pattern.compile("_files$", flags));

        if (lsExclide != null) {
            for (String s : lsExclide) {
                try {
                    this.skipDir.add(Pattern.compile(s, flags));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
