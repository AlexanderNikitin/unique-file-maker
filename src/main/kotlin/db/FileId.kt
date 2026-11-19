package db

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class FileId(
    val folder: String? = null,
    val name: String? = null,
    val extension: String? = null
) : Serializable