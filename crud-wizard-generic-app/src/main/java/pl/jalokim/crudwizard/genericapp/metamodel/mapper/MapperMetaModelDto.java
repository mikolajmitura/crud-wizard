package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.metamodels.MappingDirection;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldShouldWhenOther(field = MapperMetaModelDto.CLASS_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.BEAN_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.METHOD_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.METHOD_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.BEAN_NAME, is = NOT_NULL)
public class MapperMetaModelDto extends AdditionalPropertyMetaModelDto {

    public static final String MAPPER_SCRIPT = "mapperScript";
    public static final String CLASS_NAME = "className";
    public static final String METHOD_NAME = "methodName";
    public static final String BEAN_NAME = "beanName";

    Long id;
    String className;
    String beanName;
    String methodName;
    String mapperScript;
    @NotNull
    MappingDirection mappingDirection;
}
