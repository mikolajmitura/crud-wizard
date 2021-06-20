package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.metamodels.MappingDirection;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class MapperMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    String className;
    String beanName;
    String methodName;
    String mapperScript;
    MappingDirection mappingDirection;
}
