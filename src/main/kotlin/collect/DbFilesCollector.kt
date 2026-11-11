package collect

import db.FileEntity
import db.FileRepository

class DbFilesCollector : FilesCollector {
    private val fileRepository: FileRepository = FileRepository()
    override fun collect(foundFile: FoundFile) {
        val fileEntity = FileEntity(foundFile.path, foundFile.size, foundFile.updatedTime.toInstant())
        fileRepository.save(fileEntity)
    }
}