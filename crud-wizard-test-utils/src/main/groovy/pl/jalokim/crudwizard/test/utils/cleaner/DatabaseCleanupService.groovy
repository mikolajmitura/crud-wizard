package pl.jalokim.crudwizard.test.utils.cleaner

import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.Table
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DatabaseCleanupService {

    def tables = []
    List<String> skipTables = []

    @Autowired
    EntityManagerForCleanupProvider entityManagerProvider

    @Autowired(required = false)
    List<SkipTablesForClean> skipTablesForClean

    EntityManager entityManager

    @PostConstruct
    void postConstruct() {
        entityManager = entityManagerProvider.entityManager
        skipTablesForClean?.forEach({skipTables.addAll(it.getSkipTables())})
    }

    @EventListener(ContextRefreshedEvent)
    def fetchTablesNames() {
        def session = entityManager.unwrap(Session)
        def metamodel = session.getSessionFactory().getMetamodel()
        metamodel.entities.each {
            def table = it.getJavaType().getAnnotation(Table)
            if (table && !skipTables.contains(table.name())) {
                tables.add(table.name())
            }
        }
    }

    def cleanupDatabase() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
        tables.each {
            entityManager.createNativeQuery("DELETE FROM " + it).executeUpdate()
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }
}
