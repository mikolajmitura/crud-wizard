package pl.jalokim.crudwizard.datastorage.inmemory;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;

@RequiredArgsConstructor
public class InMemoryDataStorage implements DataStorage {

    // TODO #Next test for it

    private final String name;
    private final Map<String, EntityBag> entitiesByName = new ConcurrentHashMap<>();

    public InMemoryDataStorage() {
        this("in_memory_data_storage");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void saveEntity(ClassMetaModel classMetaModel, Map<String, Object> entity) {
        EntityBag entityBag = entitiesByName.get(classMetaModel.getName());
        if (entityBag == null) {
            entityBag = new EntityBag(classMetaModel);
            entitiesByName.put(classMetaModel.getName(), entityBag);
        }

        FieldMetaModel fieldWithId = elements(classMetaModel.getFields())
            .filter(field -> field.getAdditionalProperties().stream()
                .anyMatch(property -> property.getName().equals(FieldMetaModel.IS_ID_FIELD)))
            .getFirst();

        Object idObject = entity.get(fieldWithId.getFieldName());
        entityBag.saveEntity(idObject, entity);
    }

    @Override
    public void deleteEntity(ClassMetaModel classMetaModel, Object idObject) {
        EntityBag entityBag = entitiesByName.get(classMetaModel.getName());
        if (entityBag == null) {
            throw new EntityNotFoundException(String.format("Cannot find storage for entities: %s", classMetaModel.getName()));
        }
        entityBag.delete(idObject);
    }

    @Override
    public Map<String, Object> getEntityById(ClassMetaModel classMetaModel, Object idObject) {
        EntityBag entityBag = entitiesByName.get(classMetaModel.getName());
        return Optional.ofNullable(entityBag.getById(idObject))
            .orElseThrow(()-> new EntityNotFoundException(String.format("not exists with id: %s entity name: %s", idObject, classMetaModel.getName())));
    }

    @Override
    public Page<Map<String, Object>> findPageOfEntity(ClassMetaModel classMetaModel, Pageable pageable, Map<String, Object> queryObject) {
        // TODO how to do queries? eq, not eq, contains, in how?
        return null;
    }

    @Override
    public List<Map<String, Object>> findEntities(ClassMetaModel classMetaModel, Map<String, Object> queryObject) {
        // TODO how to do queries? eq, not eq, contains, in how?
        return null;
    }
}
