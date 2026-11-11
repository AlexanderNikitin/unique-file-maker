package db

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

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
}