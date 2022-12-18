package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import java.lang.annotation.Annotation;
import lombok.Value;

@Value
public class ResolvedValueForAnnotation {

    Annotation annotation;
    Object resolvedValue;
    MethodParameterAndAnnotation methodParameterAndAnnotation;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getExpectedAnnotation() {
        return (T) annotation;
    }

    public boolean isValueNotResolved() {
        return resolvedValue == null;
    }

    MethodParameterInfo getMethodParameterInfo() {
        return methodParameterAndAnnotation.getMethodParameterInfo();
    }
}
