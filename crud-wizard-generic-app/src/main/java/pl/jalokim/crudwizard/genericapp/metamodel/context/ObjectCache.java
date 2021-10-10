package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public class ObjectCache<K, V> {

    private final Map<K, V> objectsById = new ConcurrentHashMap<>();

    public V getById(K nullableId) {
        return Optional.ofNullable(nullableId)
            .map(id -> Optional.ofNullable(objectsById.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find object by id: " + id + " for metamodel entity: ")))
            .orElse(null);
    }

    public void put(K id, V objectValue) {
        objectsById.put(id, objectValue);
    }
}
