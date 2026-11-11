package scan

import collect.FilesCollector
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readAttributes

class FilesScanner(
    private val filesCollector: FilesCollector,
    private val fileNameFilter: FilenameFilter,
    private val skipHidden: Boolean = true
) {
    private val fileVisitor = ListFileVisitor(filesCollector, fileNameFilter, skipHidden)

    fun scan(start: String, recursive: Boolean) {
        val path = Path.of(start)
        if (recursive) {
            Files.walkFileTree(path, fileVisitor)
        } else {
            fileVisitor.preVisitDirectory(path, path.readAttributes())
            path.toFile().list().forEach {
                val file = File(it)
                try {
                    if (file.isFile) {
                        fileVisitor.visitFile(path, path.readAttributes())
                    }
                } catch (e: IOException) {
                    fileVisitor.visitFileFailed(file.toPath(), e)
                }
            }
        }
    }
}