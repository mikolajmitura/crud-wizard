package pl.jalokim.crudwizard.core.metamodels;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

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
