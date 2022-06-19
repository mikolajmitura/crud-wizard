package pl.jalokim.crudwizard.genericapp.metamodel.method;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BeanMethodMetaModel {

    String name;

    Method originalMethod;

    MethodSignatureMetaModel methodSignatureMetaModel;
}
