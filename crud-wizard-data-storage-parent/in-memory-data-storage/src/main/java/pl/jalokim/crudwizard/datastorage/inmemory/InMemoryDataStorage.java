package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@RequiredArgsConstructor
public class InMemoryDataStorage implements DataStorage {

    private final String name;

    public InMemoryDataStorage() {
        this("in_memory_data_storage");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void saveEntity(ClassMetaModel classMetaModel, Map<String, Object> entity) {

    }

    @Override
    public void deleteEntity(ClassMetaModel classMetaModel, Object idObject) {

    }

    @Override
    public Map<String, Object> getEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return null;
    }

    @Override
    public Page<Map<String, Object>> findPageOfEntity(ClassMetaModel classMetaModel, Pageable pageable, Map<String, Object> queryObject) {
        return null;
    }

    @Override
    public List<Map<String, Object>> findEntities(ClassMetaModel classMetaModel, Map<String, Object> queryObject) {
        return null;
    }
}
