package collect

import java.nio.file.attribute.FileTime

data class FoundFile(
    val path: String,
    val size: Long,
    val updatedTime: FileTime
)