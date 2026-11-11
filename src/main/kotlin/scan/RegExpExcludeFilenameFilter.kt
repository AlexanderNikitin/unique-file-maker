package scan

import java.io.File
import java.io.FilenameFilter
import java.util.regex.Pattern

class RegExpExcludeFilenameFilter(
    private val pattern: Pattern
) : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return pattern.matcher(name).find()
    }
}

