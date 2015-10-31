package FileSearcher;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtFileNameFilter implements FilenameFilter {

    private final Set<String> extsSet = new HashSet<>();

    public ExtFileNameFilter(String... exts) {
        if (exts != null) {
            for (String ext : exts) {
                this.extsSet.add(ext.toLowerCase());
            }
        }
    }

    public ExtFileNameFilter(List<String> exts) {
        if (exts != null) {
            for (String ext : exts) {
                this.extsSet.add(ext.toLowerCase());
            }
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return this.extsSet.isEmpty() || this.extsSet.contains(name.substring(name.lastIndexOf('.') + 1).toLowerCase());
    }
}
