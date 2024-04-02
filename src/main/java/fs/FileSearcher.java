package fs;

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

    private final List<String> extensions;
    private final boolean recursive;
    private final List<String> exclude;

    public FileSearcher(List<String> extensions, boolean recursive, List<String> exclude) {
        this.extensions = extensions;
        this.recursive = recursive;
        this.exclude = exclude;
    }

    public List<File> search(String[] aStartDirs) throws IOException {
        List<File> files;
        FilenameFilter filenameFilter = new ExtFileNameFilter(this.extensions);
        if (recursive) {
            ListFileVisitor fileVisitor = new ListFileVisitor(filenameFilter, this.exclude);
            Set<FileVisitOption> so = new HashSet<>();
            so.add(FileVisitOption.FOLLOW_LINKS);
            for (String startDir : aStartDirs) {
                System.out.println(startDir);
                try {
                    Files.walkFileTree(new File(startDir).toPath().toRealPath(LinkOption.NOFOLLOW_LINKS), so, Integer.MAX_VALUE, fileVisitor);
                } catch (FileSystemException e) {
                    System.out.println("FileSystemException: " + e.getFile());
                }
            }
            files = fileVisitor.files;
        } else {
            files = new ArrayList<>();
            for (String startDir : aStartDirs) {
                System.out.println(startDir);
                File[] foundFiles = new File(startDir).listFiles(filenameFilter);
                if (foundFiles == null) {
                    continue;
                }
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
