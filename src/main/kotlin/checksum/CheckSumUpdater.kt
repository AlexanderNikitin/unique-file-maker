package checksum

import Settings
import db.FileEntity
import db.FileRepository
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileNotFoundException
import java.time.Instant

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
        val file = File(fileEntity.path!!)
        if (file.exists()) {
            try {
                if (fileEntity.sha256 == null || fileEntity.updateTime!!.toEpochMilli() < file.lastModified()) {
                    if (Settings.debug) println("Считаем для ${fileEntity.path}")
                    fileEntity.sha256 = calcSha256(file)
                    fileEntity.updateTime = Instant.ofEpochMilli(file.lastModified())
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

    private fun calcSha256(file: File): String {
        return DigestUtils.sha256Hex(file.inputStream().buffered())
    }
}