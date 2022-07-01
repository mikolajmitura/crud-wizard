package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_BEAN_AND_METHOD, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.SERVICE_BEAN_AND_METHOD, is = NOT_NULL)
public class ServiceMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String SERVICE_BEAN_AND_METHOD = "serviceBeanAndMethod";
    public static final String SERVICE_SCRIPT = "serviceScript";

    Long id;

    // TODO #1 #bean_validation validation when added new service metamodel
    // verify that this bean, class, method exists
    // verify that can method arguments will be resolved correctly, expected annotations etc and types (see in DelegatedServiceMethodInvoker)
    // verify that newly added serviceMetaModel does not exists already, then use existing id
    @Valid
    BeanAndMethodDto serviceBeanAndMethod;

    String serviceScript;
}
