package pl.jalokim.crudwizard.genericapp.metamodel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@RequiredArgsConstructor
public class AbstractBaseService<E extends EntityWithId<I>, R extends JpaRepository<E, I>, I> {

    protected final R repository;
    protected @PersistenceContext(unitName = "metamodelEntityManagerFactory") EntityManager entityManager;

    public E saveNewOrLoadById(E entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            return innerSave(entity);
        } else {
            return getOne(entity.getId());
        }
    }

    public E getOne(I id) {
        return repository.getOne(id);
    }

    final E innerSave(E entity) {
       E newEntity = save(entity);
        if (entityManager.contains(newEntity)) {
            return newEntity;
        }
        return repository.save(entity);
    }

    public E save(E entity) {
        return repository.save(entity);
    }
}
