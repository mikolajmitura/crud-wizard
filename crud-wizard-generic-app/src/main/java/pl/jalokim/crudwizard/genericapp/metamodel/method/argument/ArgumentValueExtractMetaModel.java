package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArgumentValueExtractMetaModel {

    GenericMethodArgumentProvider genericMethodArgumentProvider;
    MethodParameterAndAnnotation methodParameterAndAnnotation;
}
