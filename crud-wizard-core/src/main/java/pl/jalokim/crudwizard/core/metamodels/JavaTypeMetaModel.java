package pl.jalokim.crudwizard.core.metamodels;

import com.fasterxml.jackson.databind.JavaType;
import java.lang.reflect.Type;
import lombok.Value;

@Value
public class JavaTypeMetaModel {

    Class<?> rawClass;
    Type originalType;
    JavaType jacksonJavaType;

    public static JavaTypeMetaModel createWithRawClass(Class<?> rawClass) {
        return new JavaTypeMetaModel(rawClass, null, null);
    }

    public static JavaTypeMetaModel createWithType(Type originalType, JavaType jacksonJavaType) {
        return new JavaTypeMetaModel(null, originalType, jacksonJavaType);
    }

    public boolean isRawClass() {
        return rawClass != null;
    }

    public boolean isGenericType() {
        return !isRawClass();
    }
}
