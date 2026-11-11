package scan

import java.io.File
import java.io.FilenameFilter

class AgregateFileNameFilter(
    private val filters: List<FilenameFilter>
) : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return filters.all { it.accept(dir, name) }
    }
}