package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldShouldWhenOther(field = "className", should = NULL, whenField = "serviceScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "beanName", should = NULL, whenField = "serviceScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "methodName", should = NULL, whenField = "serviceScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "serviceScript", should = NULL, whenField = "className", is = NOT_NULL)
@FieldShouldWhenOther(field = "serviceScript", should = NULL, whenField = "methodName", is = NOT_NULL)
@FieldShouldWhenOther(field = "serviceScript", should = NULL, whenField = "beanName", is = NOT_NULL)
public class ServiceMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    String className;
    String beanName;
    String methodName;
    String serviceScript;

}
