package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import javax.validation.constraints.Size;
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
    @Size(min = 3, max = 250)
    String className;

    // TODO #1 #validation validation when added new service metamodel
    // verify that this bean, class, method exists
    // verify that can method arguments will be resolved correctly, expected annotations etc and types (see in DelegatedServiceMethodInvoker)
    // verify that newly added serviceMetaModel does not exists already, then use existing id
    @Size(min = 3, max = 100)
    String beanName;

    @Size(min = 3, max = 100)
    String methodName;

    String serviceScript;
}
