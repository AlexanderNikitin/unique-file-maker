package db

import jakarta.persistence.criteria.Path
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import java.time.Instant
import java.util.*


class FileRepository {
    private val sessionFactory: SessionFactory = Configuration().addAnnotatedClass(FileEntity::class.java).buildSessionFactory()

    init {
        sessionFactory.openSession()
    }

    fun save(fileEntity: FileEntity) {
        sessionFactory.currentSession.transaction.begin()
        sessionFactory.currentSession.merge(fileEntity)
        sessionFactory.currentSession.transaction.commit()
    }

    fun findPotentialDuplicates(extensions: Array<String>? = null): Map<Long, List<FileEntity>> {
        sessionFactory.currentSession.transaction.begin()

        val criteriaBuilder = sessionFactory.currentSession.criteriaBuilder

        val query = criteriaBuilder.createTupleQuery()
        val root = query.from(FileEntity::class.java)

        val subquery = query.subquery(Long::class.java)
        val subRoot = subquery.from(FileEntity::class.java)

        val fileIdPath: Path<FileId> = root.get("fileId")
        val extensionPath: Path<String> = fileIdPath.get("extension")
        val sizePath: Path<Long> = root.get("size")
        val updateTimePath: Path<Instant> = root.get("updateTime")
        val sha256Path: Path<String> = root.get("sha256")

        val subFileIdPath: Path<FileId> = subRoot.get("fileId")
        val subSizePath: Path<Long> = subRoot.get("size")
        val subExtensionPath: Path<String> = subFileIdPath.get("extension")
        val countExpr = criteriaBuilder.count(subRoot)

        val sizePredicate = criteriaBuilder.`in`(
            sizePath,
            subquery.select(subSizePath)
                .where(
                    extensions?.let {
                        criteriaBuilder.`in`(subExtensionPath, *extensions)
                    }
                )
                .groupBy(subSizePath)
                .having(criteriaBuilder.greaterThan(countExpr, 1))
        )

        query.select(criteriaBuilder.tuple(fileIdPath, sizePath, updateTimePath, sha256Path))
            .where(
                extensions?.let {
                    criteriaBuilder.and(
                        sizePredicate,
                        criteriaBuilder.`in`(extensionPath, *extensions)
                    )
                } ?: sizePredicate
            )

        val results = sessionFactory.currentSession.createQuery(query).resultList
        val resultList = mutableMapOf<Long, MutableList<FileEntity>>()
        for (tuple in results) {
            val fileId = tuple[0] as FileId
            val size = tuple[1] as Long
            val updateTime = tuple[2] as Instant
            val sha256 = tuple[3] as String?
            resultList.putIfAbsent(size, mutableListOf())
            resultList[size]!!.add(FileEntity(fileId, size, updateTime, sha256))
        }
        sessionFactory.currentSession.transaction.rollback()
        return resultList
    }

    fun findDuplicates(): Map<UUID, List<FileEntity>> {
        sessionFactory.currentSession.transaction.begin()

        val criteriaBuilder = sessionFactory.currentSession.criteriaBuilder

        val query = criteriaBuilder.createTupleQuery()
        val root = query.from(FileEntity::class.java)

        val subquery = query.subquery(UUID::class.java)
        val subRoot = subquery.from(FileEntity::class.java)

        val fileIdPath: Path<String> = root.get("fileId")
        val sizePath: Path<Long> = root.get("size")
        val updateTimePath: Path<Instant> = root.get("updateTime")
        val sha256Path: Path<String> = root.get("sha256")
        val groupPath: Path<UUID> = root.get("groupId")

        val subGroupPath: Path<UUID> = subRoot.get("groupId")
        val countExpr = criteriaBuilder.count(subRoot)

        query.select(criteriaBuilder.tuple(fileIdPath, sizePath, updateTimePath, sha256Path, groupPath))
            .where(
                criteriaBuilder.`in`(
                    groupPath,
                    subquery.select(subGroupPath)
                        .where(criteriaBuilder.isNotNull(subGroupPath))
                        .groupBy(subGroupPath)
                        .having(criteriaBuilder.greaterThan(countExpr, 1))
                )
            )

        val results = sessionFactory.currentSession.createQuery(query).resultList
        val resultList = mutableMapOf<UUID, MutableList<FileEntity>>()
        for (tuple in results) {
            val fileId = tuple[0] as FileId
            val size = tuple[1] as Long
            val updateTime = tuple[2] as Instant
            val sha256 = tuple[3] as String?
            val group = tuple[4] as UUID
            resultList.putIfAbsent(group, mutableListOf())
            resultList[group]!!.add(FileEntity(fileId, size, updateTime, sha256, groupId = group))
        }
        sessionFactory.currentSession.transaction.rollback()
        return resultList
    }
}