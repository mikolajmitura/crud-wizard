package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import lombok.Value;

@Value
public class ArgumentValueExtractMetaModel {

    GenericMethodArgumentProvider genericMethodArgumentProvider;
    MethodParameterAndAnnotation methodParameterAndAnnotation;
}
