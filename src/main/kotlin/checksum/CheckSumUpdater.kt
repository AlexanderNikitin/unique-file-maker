package checksum

import Settings
import db.FileEntity
import db.FileRepository
import org.apache.commons.codec.digest.DigestUtils
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.inputStream

class CheckSumUpdater(
    val fileEntity: FileEntity,
    private val fileRepository: FileRepository
) {
    val sum: String?
        get() = if (valid) fileEntity.sha256 else null

    val valid: Boolean
        get() {
            validate()
            return !(fileEntity.fnf ?: false || fileEntity.calcErr ?: false) && fileEntity.sha256 != null
        }

    fun validate() {
        val fileId = fileEntity.fileId
        val path = Path.of(fileId.folder!!, fileId.name)
        if (path.exists()) {
            try {
                val lastModified = path.getLastModifiedTime().toInstant()
                if (fileEntity.sha256 == null || fileEntity.updateTime?.isBefore(lastModified) ?: true) {
                    if (Settings.debug) println("Считаем для $path")
                    fileEntity.sha256 = calcSha256(path)
                    fileEntity.updateTime = lastModified
                    fileEntity.fnf = false
                    fileEntity.calcErr = false
                }
            } catch (e: Exception) {
                fileEntity.calcErr = true
                if (Settings.debug) e.printStackTrace()
            } catch (e: FileNotFoundException) {
                fileEntity.fnf = true
                if (Settings.debug) e.printStackTrace()
            }
        } else {
            fileEntity.fnf = true
        }
        fileRepository.save(fileEntity)
    }

    private fun calcSha256(path: Path): String {
        return DigestUtils.sha256Hex(path.inputStream().buffered())
    }
}