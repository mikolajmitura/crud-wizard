package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldShouldWhenOther(field = MapperMetaModelDto.CLASS_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.BEAN_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.METHOD_NAME, should = NULL, whenField = MapperMetaModelDto.MAPPER_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.METHOD_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MapperMetaModelDto.BEAN_NAME, is = NOT_NULL)
public class MapperMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String MAPPER_SCRIPT = "mapperScript";
    public static final String CLASS_NAME = "className";
    public static final String METHOD_NAME = "methodName";
    public static final String BEAN_NAME = "beanName";

    Long id;
    String className;
    String beanName;
    String methodName;
    String mapperScript;

}
