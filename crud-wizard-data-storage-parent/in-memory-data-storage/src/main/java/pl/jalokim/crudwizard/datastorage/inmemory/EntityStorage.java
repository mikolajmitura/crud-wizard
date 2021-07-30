package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;

@Value
public class EntityStorage {

    ClassMetaModel forModel;
    IdGenerators idGenerators;
    Map<Object, Map<String, Object>> entitiesById = new ConcurrentHashMap<>();

    public synchronized Map<String, Object> getById(Object idObject) {
        return entitiesById.get(idObject);
    }

    public synchronized Object saveEntity(Object idObject, FieldMetaModel fieldWithId, Map<String, Object> entity) {
        Object idObjectToReturn = idObject;
        if (idObjectToReturn == null) {
            idObjectToReturn = idGenerators.getNextFor(fieldWithId.getFieldType().getRealClass());
            entity.put(fieldWithId.getFieldName(), idObjectToReturn);
        } else {
            if (entitiesById.containsKey(idObjectToReturn)) {
                delete(idObjectToReturn);
            }
        }
        entitiesById.put(idObjectToReturn, entity);

        return idObjectToReturn;
    }

    public synchronized void delete(Object idObject) {
        if (!entitiesById.containsKey(idObject)) {
            throw new EntityNotFoundException(String.format("not exists with id: %s entity name: %s", idObject, forModel.getName()));
        }
        entitiesById.remove(idObject);
    }
}
