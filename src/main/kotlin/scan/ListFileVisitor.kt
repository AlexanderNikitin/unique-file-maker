package scan

import collect.FoundFile
import collect.FilesCollector
import java.io.FilenameFilter
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.io.path.isHidden
import kotlin.io.path.name

class ListFileVisitor(
    private val filesCollector: FilesCollector,
    private val fileNameFilter: FilenameFilter,
    private val skipHidden: Boolean = true
) : SimpleFileVisitor<Path>() {
    private val visitedDirs: MutableSet<String> = mutableSetOf()

    override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        if (!attributes.isDirectory) {
            return FileVisitResult.SKIP_SUBTREE
        }
        if (skipHidden && path.isHidden()) {
            return FileVisitResult.SKIP_SUBTREE
        }
        val canonicalPath = path.toFile().canonicalPath
        if (visitedDirs.contains(canonicalPath)) {
            return FileVisitResult.SKIP_SUBTREE
        }
        return if (fileNameFilter.accept(path.parent.toFile(), path.name)) {
            visitedDirs.add(canonicalPath)
            FileVisitResult.CONTINUE
        } else {
            FileVisitResult.SKIP_SUBTREE
        }
    }

    override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        if (skipHidden && path.isHidden()) {
            return FileVisitResult.CONTINUE
        }
        if (attributes.isRegularFile) {
            filesCollector.collect(FoundFile(
                path.absolutePathString(),
                attributes.size(),
                attributes.lastModifiedTime()))
        }
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(dir: Path, exc: IOException): FileVisitResult {
        if (exc.javaClass == java.nio.file.NoSuchFileException::class.java) {
            println("Не найден файл $dir")
        }
        if (Settings.debug) Throwable(exc).printStackTrace()
        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        exc?.let {
            Throwable(it).printStackTrace()
        }
        return FileVisitResult.CONTINUE
    }
}
