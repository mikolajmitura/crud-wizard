package pl.jalokim.crudwizard.genericapp.metamodel;

import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class BaseService<E extends BaseEntity, R extends JpaRepository<E, Long>>
    extends AbstractBaseService<E, R, Long> {

    public BaseService(R repository, EntityManager entityManager) {
        super(repository, entityManager);
    }
}
