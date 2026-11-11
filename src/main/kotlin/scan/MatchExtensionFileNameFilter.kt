package scan

import java.io.File
import java.io.FilenameFilter

class MatchExtensionFileNameFilter(
    private val extension: String
) : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return File(name).extension == extension
    }
}