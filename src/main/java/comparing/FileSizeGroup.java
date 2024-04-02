package comparing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileSizeGroup {

    private final Stack<FileInReadingGroup> stack = new Stack<>();

    public FileSizeGroup(List<File> files) {
        if (files != null && !files.isEmpty()) {
            List<File> filtered = new ArrayList<>();
            long size = -1;
            for (File file : files) {
                if (size >= 0) {
                    if (file.length() == size) {
                        filtered.add(file);
                    } else {
                        System.out.println("Different size: " + file.toString());
                    }
                } else {
                    size = file.length();
                    filtered.add(file);
                }
            }
            try {
                this.stack.push(FileInReadingGroup.initFromFiles(filtered));
            } catch (Exception e) {
                System.err.println(e.toString());
                System.err.println(e.getMessage());
            }
        }
    }

    public List<File[]> getGroups() throws IOException {
        List<File[]> result = new ArrayList<>();
        while (!this.stack.empty()) {
            try (FileInReadingGroup readingGroup = this.stack.pop()) {
                while ((readingGroup.count() > 1) && readingGroup.read()) {
                    for (FileInReadingGroup gr : readingGroup.separateDiff()) {
                        stack.push(gr);
                    }
                }
                result.add(readingGroup.fiels());
            }
        }
        return result;
    }
}
