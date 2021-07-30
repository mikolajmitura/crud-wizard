package pl.jalokim.crudwizard.core.datastorage;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public interface DataStorage {

    default DataStorageTransactionProvider getTransactionProvider() {
        return null;
    }

    String getName();

    /**
     * It save RawEntity and returns id of saved object
     *
     * @param classMetaModel metamodel for entity
     * @param entity value of entity
     * @return id of saved object
     */
    Object saveEntity(ClassMetaModel classMetaModel, Map<String, Object> entity);

    void deleteEntity(ClassMetaModel classMetaModel, Object idObject);

    Map<String, Object> getEntityById(ClassMetaModel classMetaModel, Object idObject);

    Page<Map<String, Object>> findPageOfEntity(ClassMetaModel classMetaModel, Pageable pageable, Map<String, Object> queryObject);

    List<Map<String, Object>> findEntities(ClassMetaModel classMetaModel, Map<String, Object> queryObject);

    default String infoDataStorage() {
        return String.format("%s %s", getName(), getClassName());
    }

    default String getClassName() {
        return getClass().getCanonicalName();
    }

}
