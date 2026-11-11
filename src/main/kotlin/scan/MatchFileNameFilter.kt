package scan

import java.io.File
import java.io.FilenameFilter

class MatchFileNameFilter(
    private val fileName: String
): FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return fileName != name
    }
}