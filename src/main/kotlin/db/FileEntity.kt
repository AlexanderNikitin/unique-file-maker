package db

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "file")
data class FileEntity(
    @Id val fileId: FileId = FileId(),
    @Column(name = "size")
    var size: Long? = null,
    @Column(name = "update_time")
    var updateTime: Instant? = null,
    @Column(name = "sha256")
    var sha256: String? = null,
    @Column(name = "fnf")
    var fnf: Boolean? = false,
    @Column(name = "calc_err")
    var calcErr: Boolean? = false,
    @Column(name = "group_id")
    var groupId: UUID? = null,
    @Column(name = "error_message")
    var errorMessage: String? = null,
    @Column(name = "delete")
    var delete: Boolean? = null
)