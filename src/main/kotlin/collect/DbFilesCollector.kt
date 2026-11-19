package collect

import db.FileEntity
import db.FileId
import db.FileRepository
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name

class DbFilesCollector : FilesCollector {
    private val fileRepository: FileRepository = FileRepository()
    override fun collect(path: Path) {
        val fileEntity = FileEntity(
            FileId(
                path.parent.absolutePathString(),
                path.name,
                path.extension
            ), path.fileSize(), path.getLastModifiedTime().toInstant()
        )
        fileRepository.save(fileEntity)
    }
}