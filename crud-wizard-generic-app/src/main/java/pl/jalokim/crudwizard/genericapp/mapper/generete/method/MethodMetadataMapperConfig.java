package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Builder
@Value
public class MethodMetadataMapperConfig {

    String methodName;
    ClassMetaModel argumentClassMetaModel;
    ClassMetaModel returnClassMetaModel;
}
