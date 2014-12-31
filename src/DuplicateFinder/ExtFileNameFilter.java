package DuplicateFinder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtFileNameFilter implements FilenameFilter {

    private final Set<String> extsSet;

    ExtFileNameFilter(List<String> exts) {
        this.extsSet = new HashSet<>();
        if (exts != null) {
            this.extsSet.addAll(exts);
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return this.extsSet.isEmpty() || this.extsSet.contains(name.substring(name.lastIndexOf('.') + 1));
    }
}
