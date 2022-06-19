package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Builder
@Value
public class MethodMetadataMapperConfig {

    String methodName;
    ClassMetaModel argumentClassMetaModel;
    ClassMetaModel returnClassMetaModel;
}
