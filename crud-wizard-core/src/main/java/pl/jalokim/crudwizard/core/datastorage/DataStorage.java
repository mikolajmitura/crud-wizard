package pl.jalokim.crudwizard.core.datastorage;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public interface DataStorage {

    default DataStorageTransactionProvider getTransactionProvider() {
        return null;
    }

    String getName();

    /**
     * It save RawEntity and returns id of saved object. It can be used to update as well.
     * When entity has not null id value then it will try update otherwise
     * will generate new id and save as new object in storage.
     *
     * @param classMetaModel metamodel for entity
     * @param entity value of entity
     * @return id of saved object
     */
    Object saveOrUpdate(ClassMetaModel classMetaModel, Object entity);

    Optional<Object> getOptionalEntityById(ClassMetaModel classMetaModel, Object idObject);

    Page<Object> findPageOfEntity(Pageable pageable, DataStorageQuery query);

    List<Object> findEntities(DataStorageQuery query);

    void innerDeleteEntity(ClassMetaModel classMetaModel, Object idObject);

    default void deleteEntity(ClassMetaModel classMetaModel, Object idObject) {
        getOptionalEntityById(classMetaModel, idObject)
            .ifPresentOrElse(foundEntity -> innerDeleteEntity(classMetaModel, idObject),
                () -> {
                    throw new EntityNotFoundException(idObject, classMetaModel.getName());
                }
            );
    }

    void delete(DataStorageQuery query);

    default Object getEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return getOptionalEntityById(classMetaModel, idObject)
            .orElseThrow(() -> new EntityNotFoundException(idObject, classMetaModel.getName()));
    }

    default String infoDataStorage() {
        return String.format("%s %s", getName(), getClassName());
    }

    default String getClassName() {
        return getClass().getCanonicalName();
    }

    long count(DataStorageQuery query);
}
