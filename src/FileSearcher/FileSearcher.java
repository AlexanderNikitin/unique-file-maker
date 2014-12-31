package FileSearcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileSearcher {

    private final List<String> exts;
    private final boolean bRecursive;
    private final List<String> lsExclide;

    public FileSearcher(List<String> exts, boolean bRecursive, List<String> lsExclide) {
        this.exts = exts;
        this.bRecursive = bRecursive;
        this.lsExclide = lsExclide;
    }

    public List<File> search(String[] aStartDirs) throws IOException {
        List<File> files;
        FilenameFilter fnf = new ExtFileNameFilter(this.exts);
        if (bRecursive) {
            ListFileVisitor lfv = new ListFileVisitor(fnf, this.lsExclide);
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
        return files;
    }
}
