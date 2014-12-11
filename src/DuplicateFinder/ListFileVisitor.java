package DuplicateFinder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            Logger.getLogger(ListFileVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attr) {
        File file = path.toFile();
        if (file.length() > 0 && this.fnf.accept(null, file.getName())) {
            this.files.add(file);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path dir, IOException exc) {
        //System.out.println("err: " + dir.toAbsolutePath().toString());
        return CONTINUE;
    }

    public ListFileVisitor(FilenameFilter fnf) {
        this.fnf = fnf;
        this.setVisitedDirs = new HashSet<>();

        this.skipDir = new ArrayList<>();
        this.skipDir.add(Pattern.compile("^backup_\\d+$"));
        this.skipDir.add(Pattern.compile("_files$"));
    }
}
