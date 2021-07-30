package pl.jalokim.crudwizard.core.datastorage;

import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RawEntityObjectUtils {

    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(Map<String, ?> map, String fieldName) {
        return (T) map.get(fieldName);
    }
}
