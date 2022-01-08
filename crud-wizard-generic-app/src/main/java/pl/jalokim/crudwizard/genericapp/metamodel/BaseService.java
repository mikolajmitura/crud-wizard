package pl.jalokim.crudwizard.genericapp.metamodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@RequiredArgsConstructor
public abstract class BaseService<E extends BaseEntity, R extends JpaRepository<E, Long>>  {

    protected final R repository;

    public E saveNewOrLoadById(E entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
            return save(entity);
        } else {
            return getOne(entity.getId());
        }
    }

    public E getOne(Long id) {
        return repository.getOne(id);
    }

    public E save(E entity) {
        return repository.save(entity);
    }
}
