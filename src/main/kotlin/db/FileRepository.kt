package db

import jakarta.persistence.criteria.Path
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import java.time.Instant
import java.util.UUID


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

    fun findPotentialDuplicates(): Map<Long, List<FileEntity>> {
        sessionFactory.currentSession.transaction.begin()

        val criteriaBuilder = sessionFactory.currentSession.criteriaBuilder

        val query = criteriaBuilder.createTupleQuery()
        val root = query.from(FileEntity::class.java)

        val subquery = query.subquery(Long::class.java)
        val subRoot = subquery.from(FileEntity::class.java)

        val pathPath: Path<String> = root.get("path")
        val sizePath: Path<Long> = root.get("size")
        val updateTimePath: Path<Instant> = root.get("updateTime")
        val sha256Path: Path<String> = root.get("sha256")

        val subSizePath: Path<Long> = subRoot.get("size")
        val countExpr = criteriaBuilder.count(subRoot)

        query.select(criteriaBuilder.tuple(pathPath, sizePath, updateTimePath, sha256Path))
            .where(
                criteriaBuilder.`in`(
                    sizePath,
                    subquery.select(subSizePath)
                        .groupBy(subSizePath)
                        .having(criteriaBuilder.greaterThan(countExpr, 1))
                )
            )

        val results = sessionFactory.currentSession.createQuery(query).resultList
        val resultList = mutableMapOf<Long, MutableList<FileEntity>>()
        for (tuple in results) {
            val path = tuple[0] as String
            val size = tuple[1] as Long
            val updateTime = tuple[2] as Instant
            val sha256 = tuple[3] as String?
            resultList.putIfAbsent(size, mutableListOf())
            resultList[size]!!.add(FileEntity(path, size, updateTime, sha256))
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

        val pathPath: Path<String> = root.get("path")
        val sizePath: Path<Long> = root.get("size")
        val updateTimePath: Path<Instant> = root.get("updateTime")
        val sha256Path: Path<String> = root.get("sha256")
        val groupPath: Path<UUID> = root.get("groupId")

        val subGroupPath: Path<UUID> = subRoot.get("groupId")
        val countExpr = criteriaBuilder.count(subRoot)

        query.select(criteriaBuilder.tuple(pathPath, sizePath, updateTimePath, sha256Path, groupPath))
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
            val path = tuple[0] as String
            val size = tuple[1] as Long
            val updateTime = tuple[2] as Instant
            val sha256 = tuple[3] as String?
            val group = tuple[4] as UUID
            resultList.putIfAbsent(group, mutableListOf())
            resultList[group]!!.add(FileEntity(path, size, updateTime, sha256, groupId = group))
        }
        sessionFactory.currentSession.transaction.rollback()
        return resultList
    }
}