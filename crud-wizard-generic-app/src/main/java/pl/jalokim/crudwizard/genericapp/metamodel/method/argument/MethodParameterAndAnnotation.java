package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import lombok.Value;

@Value
public class MethodParameterAndAnnotation {

    Annotation annotation;
    MethodParameterInfo methodParameterInfo;
    Method forMethod;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getExpectedAnnotation() {
        return (T) annotation;
    }
}
