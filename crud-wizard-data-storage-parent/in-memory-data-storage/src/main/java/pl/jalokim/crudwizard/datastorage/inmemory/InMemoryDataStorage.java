package pl.jalokim.crudwizard.datastorage.inmemory;

import static pl.jalokim.crudwizard.core.utils.DataFieldsHelper.getFieldValue;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory.InMemoryDsQueryRunner;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

@RequiredArgsConstructor
public class InMemoryDataStorage implements DataStorage {

    public static final String DEFAULT_DS_NAME = "in_memory_data_storage";

    private final String name;
    @Getter
    private final Map<String, EntityStorage> entitiesByName = new ConcurrentHashMap<>();
    private final IdGenerators idGenerators;
    private final InMemoryDsQueryRunner inMemoryDsQueryRunner;

    public InMemoryDataStorage(IdGenerators idGenerators, InMemoryDsQueryRunner inMemoryDsQueryRunner) {
        this(DEFAULT_DS_NAME, idGenerators, inMemoryDsQueryRunner);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object saveOrUpdate(ClassMetaModel classMetaModel, Object entity) {
        String meteModelName = classMetaModel.getTypeDescription();
        EntityStorage entityBag = entitiesByName.get(meteModelName);
        if (entityBag == null) {
            entityBag = new EntityStorage(classMetaModel, idGenerators);
            entitiesByName.put(meteModelName, entityBag);
        }

        FieldMetaModel fieldWithId = classMetaModel.getIdFieldMetaModel();

        Object idObject = getFieldValue(entity, fieldWithId.getFieldName());
        return entityBag.saveEntity(idObject, fieldWithId, entity);
    }

    @Override
    public Optional<Object> getOptionalEntityById(ClassMetaModel classMetaModel, Object idObject) {
        return Optional.ofNullable(entitiesByName.get(classMetaModel.getTypeDescription()))
            .map(entityStorage -> entityStorage.getById(idObject));
    }

    @Override
    public Page<Object> findPageOfEntity(Pageable pageable, DataStorageQuery query) {
        DataStorageQuery withoutPageable = query.toBuilder()
            .pageable(null)
            .build();

        List<Object> foundAll = findEntities(withoutPageable);

        long totalElements = foundAll.size();

        List<Object> pageContent = elements(foundAll)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .asList();

        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    @Override
    public List<Object> findEntities(DataStorageQuery query) {
        ClassMetaModel selectFromClassMetaModel = query.getSelectFrom();
        return Optional.ofNullable(entitiesByName.get(selectFromClassMetaModel.getTypeDescription()))
            .map(entityStorage -> inMemoryDsQueryRunner.runQuery(entityStorage.fetchStream(), query))
            .orElse(List.of());
    }

    @Override
    public void innerDeleteEntity(ClassMetaModel classMetaModel, Object idObject) {
        EntityStorage entityBag = entitiesByName.get(classMetaModel.getTypeDescription());
        if (entityBag == null) {
            throw new EntityNotFoundException(String.format("Cannot find storage for entities: %s", classMetaModel.getTypeDescription()));
        }
        entityBag.delete(idObject);
    }

    @Override
    public void delete(DataStorageQuery query) {
        ClassMetaModel classMetaModel = query.getSelectFrom();
        FieldMetaModel fieldWithId = classMetaModel.getIdFieldMetaModel();
        findEntities(query).forEach(entry -> {
            Object idObject = getFieldValue(entry, fieldWithId.getFieldName());
            deleteEntity(classMetaModel, idObject);
        });
    }

    @Override
    public long count(DataStorageQuery query) {
        return findEntities(query).size();
    }

    public void clear() {
        entitiesByName.clear();
    }
}
