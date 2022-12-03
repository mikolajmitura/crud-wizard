package pl.jalokim.crudwizard.genericapp.metamodel.method;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodSignatureMetaModel {

    List<MethodArgumentMetaModel> methodArguments;

    JavaTypeMetaModel returnType;
}
