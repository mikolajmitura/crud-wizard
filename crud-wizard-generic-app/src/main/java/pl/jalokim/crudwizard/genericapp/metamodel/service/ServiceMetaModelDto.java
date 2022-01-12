package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@FieldShouldWhenOther(field = ServiceMetaModelDto.CLASS_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.BEAN_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.METHOD_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.METHOD_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.BEAN_NAME, is = NOT_NULL)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class ServiceMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String CLASS_NAME = "className";
    public static final String SERVICE_SCRIPT = "serviceScript";
    public static final String METHOD_NAME = "methodName";
    public static final String BEAN_NAME = "beanName";

    Long id;

    @ClassExists
    String className;
    String beanName;
    String methodName;
    String serviceScript;
}
