package pl.jalokim.crudwizard.genericapp.metamodel.method;

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

    public static JavaTypeMetaModel createWithType(Class<?> rawClass, Type originalType, JavaType jacksonJavaType) {
        return new JavaTypeMetaModel(rawClass, originalType, jacksonJavaType);
    }

    public boolean isRawClass() {
        return originalType == null && jacksonJavaType == null;
    }

    public boolean isGenericType() {
        return !isRawClass();
    }

    @Override
    public String toString() {
        if (isRawClass()) {
            return "rawClass=" + rawClass.getCanonicalName();
        } else {
            return "generic type=" + originalType.toString();
        }
    }
}
