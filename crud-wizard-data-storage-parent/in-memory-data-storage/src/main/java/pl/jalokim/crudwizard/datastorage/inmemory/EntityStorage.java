package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.RawEntity;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;

@Value
public class EntityStorage {

    ClassMetaModel forModel;
    IdGenerators idGenerators;
    Map<Object, RawEntity> entitiesById = new ConcurrentHashMap<>();

    public synchronized RawEntity getById(Object idObject) {
        return entitiesById.get(idObject);
    }

    public synchronized Object saveEntity(Object idObject, FieldMetaModel fieldWithId, RawEntity entity) {
        if (idObject != null) {
            if (entitiesById.containsKey(idObject)) {
                delete(idObject);
            }
        } else {
            idObject = idGenerators.getNextFor(fieldWithId.getFieldType().getRealClass());
            entity.field(fieldWithId.getFieldName(), idObject);
        }
        entitiesById.put(idObject, entity);

        return idObject;
    }

    public synchronized void delete(Object idObject) {
        if (!entitiesById.containsKey(idObject)) {
            throw new EntityNotFoundException(String.format("not exists with id: %s entity name: %s", idObject, forModel.getName()));
        }
        entitiesById.remove(idObject);
    }
}
