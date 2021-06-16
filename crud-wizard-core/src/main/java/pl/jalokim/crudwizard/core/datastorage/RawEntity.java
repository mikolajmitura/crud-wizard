package pl.jalokim.crudwizard.core.datastorage;

import java.util.HashMap;
import java.util.Map;

public class RawEntity extends HashMap<String, Object> {

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(String fieldName) {
        return (T) get(fieldName);
    }

    public static RawEntity newRawEntity() {
        return new RawEntity();
    }

    public RawEntity field(String name, Object value) {
        put(name, value);
        return this;
    }

    public static RawEntity fromMap(Map<String, Object> map) {
        var rawEntity = newRawEntity();
        map.forEach(rawEntity::field);
        return rawEntity;
    }
}
