package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Value;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
public class EntityBag {

    ClassMetaModel forModel;
    Map<Object, Map<String, Object>> entitiesById = new ConcurrentHashMap<>();

    public synchronized Map<String, Object> getById(Object idObject) {
        return entitiesById.get(idObject);
    }

    public synchronized Object saveEntity(Object idObject, Map<String, Object> entity) {
        if (idObject != null) {
            if (entitiesById.containsKey(idObject)) {
                delete(idObject);
            }
        } else {
            idObject = null; // TODO #NEXT some id generators for String will be uuid for numbers incremental atomic etc
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
