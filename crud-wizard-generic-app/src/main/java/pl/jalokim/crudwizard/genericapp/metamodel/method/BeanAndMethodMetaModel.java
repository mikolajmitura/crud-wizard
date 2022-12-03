package pl.jalokim.crudwizard.genericapp.metamodel.method;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BeanAndMethodMetaModel {

    String className;
    String beanName;
    String methodName;

    Method originalMethod;

    MethodSignatureMetaModel methodSignatureMetaModel;
}
