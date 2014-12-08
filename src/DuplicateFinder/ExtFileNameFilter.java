package DuplicateFinder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Pattern;

public class ExtFileNameFilter implements FilenameFilter {

    private final Pattern p;

    public ExtFileNameFilter(String exts) {
        this.p = exts == null || exts.isEmpty() ? null : Pattern.compile(".*\\.(" + exts + ")$", Pattern.CASE_INSENSITIVE);
    }

    ExtFileNameFilter(List<String> exts) {
        this(exts == null || exts.isEmpty() ? null : String.join("|", exts));
    }

    @Override
    public boolean accept(File dir, String name) {
        return this.p == null ? true : this.p.matcher(name).matches();
    }
}
