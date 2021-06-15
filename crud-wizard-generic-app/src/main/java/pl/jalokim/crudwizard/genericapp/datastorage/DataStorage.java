package pl.jalokim.crudwizard.genericapp.datastorage;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public interface DataStorage {

    default DataStorageTransactionProvider getTransactionProvider() {
        return null;
    }

    String getName();

    void saveEntity(ClassMetaModel classMetaModel, Map<String, Object> entity);

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
