package pl.jalokim.crudwizard.test.utils.cleaner

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import org.springframework.stereotype.Component

@Component
class EntityManagerForCleanupProvider {

    @PersistenceContext
    EntityManager entityManager
}
