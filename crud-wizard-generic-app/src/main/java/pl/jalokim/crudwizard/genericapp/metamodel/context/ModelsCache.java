package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public class ModelsCache<M> {

    private Map<Long, M> modelsById = new ConcurrentHashMap<>();

    public M getById(Long nullableId) {
        return Optional.ofNullable(nullableId)
            .map(id -> Optional.ofNullable(modelsById.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find metamodel by id: " + id + " for metamodel entity: ")))
            .orElse(null);
    }

    public void put(Long id, M metamodel) {
        modelsById.put(id, metamodel);
    }
}
