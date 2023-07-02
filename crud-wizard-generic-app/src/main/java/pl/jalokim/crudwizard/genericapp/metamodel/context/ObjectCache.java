package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public class ObjectCache<K, V> {

    private final Map<K, V> objectsById = new ConcurrentHashMap<>();

    public V findById(K nullableId) {
        return Optional.ofNullable(nullableId)
            .flatMap(id -> Optional.ofNullable(objectsById.get(id)))
            .orElse(null);
    }

    public V getById(K nullableId) {
        return Optional.ofNullable(nullableId)
            .map(id -> Optional.ofNullable(objectsById.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find object by id: " + id + " for metamodel entity: ")))
            .orElse(null);
    }

    public void put(K id, V objectValue) {
        objectsById.put(id, objectValue);
    }

    public List<V> fetchAll() {
        return elements(objectsById.values()).asList();
    }

    public V findOneBy(Predicate<V> findBy) {
        List<V> elements = elements(objectsById.values())
            .filter(findBy)
            .asList();
        if (elements.size() == 1) {
            return elements.get(0);
        }
        if (elements.size() == 0) {
            return null;
        }
        throw new IllegalStateException("found more than one element: elements: " + elements);
    }

    public boolean idExists(K id) {
        return objectsById.get(id) != null;
    }
}
