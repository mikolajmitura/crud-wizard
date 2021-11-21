package pl.jalokim.crudwizard.datastorage.inmemory;

import static pl.jalokim.crudwizard.core.utils.DataFieldsHelper.getFieldValue;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;

@RequiredArgsConstructor
public class InMemoryDataStorage implements DataStorage {

    public static final String DEFAULT_DS_NAME = "in_memory_data_storage";
    private final String name;
    private final Map<String, EntityStorage> entitiesByName = new ConcurrentHashMap<>();
    private final IdGenerators idGenerators;

    public InMemoryDataStorage(IdGenerators idGenerators) {
        this(DEFAULT_DS_NAME, idGenerators);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object saveEntity(ClassMetaModel classMetaModel, Object entity) {
        EntityStorage entityBag = entitiesByName.get(classMetaModel.getName());
        if (entityBag == null) {
            entityBag = new EntityStorage(classMetaModel, idGenerators);
            entitiesByName.put(classMetaModel.getName(), entityBag);
        }

        FieldMetaModel fieldWithId = elements(classMetaModel.fetchAllFields())
            .filter(field -> field.getAdditionalProperties().stream()
                .anyMatch(property -> FieldMetaModel.IS_ID_FIELD.equals(property.getName())))
            .findFirst()
            .orElseThrow(() -> new TechnicalException("Cannot find field annotated as 'is_id_field' for classMetaModel with id: "
                + classMetaModel.getId() + " and name: " + classMetaModel.getName()));

        Object idObject = getFieldValue(entity, fieldWithId.getFieldName());
        return entityBag.saveEntity(idObject, fieldWithId, entity);
    }

    @Override
    public Optional<Object> getOptionalEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return Optional.ofNullable(entitiesByName.get(classMetaModel.getName()))
            .map(entityStorage -> entityStorage.getById(idObject));
    }

    @Override
    public Page<Object> findPageOfEntity(ClassMetaModel classMetaModel, Pageable pageable, DataStorageQuery query) {
        // TODO how to do queries? eq, not eq, contains, in how?
        return null;
    }

    @Override
    public List<Object> findEntities(ClassMetaModel classMetaModel, DataStorageQuery query) {
        // TODO how to do queries? eq, not eq, contains, in how?
        return null;
    }

    @Override
    public void innerDeleteEntity(ClassMetaModel classMetaModel, Object idObject) {
        EntityStorage entityBag = entitiesByName.get(classMetaModel.getName());
        if (entityBag == null) {
            throw new EntityNotFoundException(String.format("Cannot find storage for entities: %s", classMetaModel.getName()));
        }
        entityBag.delete(idObject);
    }

    public void clear() {
        entitiesByName.clear();
    }
}
