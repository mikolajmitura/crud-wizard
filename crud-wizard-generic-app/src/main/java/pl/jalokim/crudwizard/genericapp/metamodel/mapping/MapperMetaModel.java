package pl.jalokim.crudwizard.genericapp.metamodel.mapping;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class MapperMetaModel extends ParentMetaModel {

    Long id;
    String realClassName;
    String realMethodName;
    String mapperScript;
    MappingDirection mappingDirection;
}
