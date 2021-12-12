package pl.jalokim.crudwizard.core.datastorage.query;

import java.util.Objects;
import lombok.Value;
import pl.jalokim.utils.collection.Elements;

@Value
public class ExpressionArgument {

    String fullPath;
    Object realValue;
    DataStorageQuery dataStorageQuery;

    public static ExpressionArgument buildForPath(String path) {
        return new ExpressionArgument(path, null, null);
    }

    public static ExpressionArgument buildForValue(Object realValue) {
        return new ExpressionArgument(null, realValue, null);
    }

    public static ExpressionArgument buildForDataStorageQuery(DataStorageQuery dataStorageQuery) {
        return new ExpressionArgument(null, null, dataStorageQuery);
    }

    @Override
    public String toString() {
        return "ExpressionArgument(" + Elements.elements(printWhenNotNull("fullPath", fullPath),
            printWhenNotNull("realValue", realValue),
            printWhenNotNull("dataStorageQuery", dataStorageQuery))
            .filter(Objects::nonNull)
            .asConcatText(", ") + ")";
    }

    private String printWhenNotNull(String fieldName, Object value) {
        return value != null ? fieldName + "=" + value : null;
    }
}
