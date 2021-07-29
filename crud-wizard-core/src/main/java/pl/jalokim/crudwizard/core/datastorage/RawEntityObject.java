package pl.jalokim.crudwizard.core.datastorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RawEntityObject extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(String fieldName) {
        return (T) get(fieldName);
    }

    public static RawEntityObject newRawEntity() {
        return new RawEntityObject();
    }

    public RawEntityObject field(String name, Object value) {
        put(name, value);
        return this;
    }

    public static RawEntityObject fromMap(Map<String, ?> fromMap) {
        var rawEntity = newRawEntity();
        Optional.ofNullable(fromMap)
            .ifPresent(map -> map.forEach(rawEntity::field));
        return rawEntity;
    }
}
