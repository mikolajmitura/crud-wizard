package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class MapperMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;
    Object mapperInstance;
    String className;
    String beanName;
    String methodName;
    BeanMethodMetaModel methodMetaModel;

    // TODO script will be loaded to mapperInstance in lazy way and cached then
    String mapperScript;
    MappingDirection mappingDirection;
}
