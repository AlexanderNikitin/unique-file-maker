package db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "file")
class FileEntity(
    @Id @Column(
        name = "path",
        unique = true,
        updatable = false,
        nullable = false,
        length = 256 * 256
    )
    var path: String? = null,
    @Column(name = "size")
    var size: Long? = null,
    @Column(name = "update_time")
    var updateTime: Instant? = null
)