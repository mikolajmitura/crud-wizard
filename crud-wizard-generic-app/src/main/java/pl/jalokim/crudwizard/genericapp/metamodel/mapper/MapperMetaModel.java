package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class MapperMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    String realClassName;
    String realMethodName;
    String mapperScript;
    MappingDirection mappingDirection;
}
