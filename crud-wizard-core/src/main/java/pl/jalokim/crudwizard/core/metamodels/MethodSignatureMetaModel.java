package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodSignatureMetaModel {

    List<MethodArgumentMetaModel> methodArguments;

    JavaTypeMetaModel returnType;
}
