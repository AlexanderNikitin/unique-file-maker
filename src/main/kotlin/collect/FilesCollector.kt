package collect

import java.nio.file.Path

interface FilesCollector {
    fun collect(path: Path)
}