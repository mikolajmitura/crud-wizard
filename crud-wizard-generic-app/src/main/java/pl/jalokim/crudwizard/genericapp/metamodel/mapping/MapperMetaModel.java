package pl.jalokim.crudwizard.genericapp.metamodel.mapping;

import lombok.Value;

@Value
public class MapperMetaModel {

    Long mapperMetaModelId;
    Long mapperRealClassId;
    String mapperScript;
    MappingDirection mappingDirection;
}
