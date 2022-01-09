package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@FieldShouldWhenOther(field = ServiceMetaModelDto.CLASS_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.BEAN_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.METHOD_NAME, should = NULL, whenField = ServiceMetaModelDto.SERVICE_SCRIPT, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.METHOD_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ServiceMetaModelDto.SERVICE_SCRIPT, should = NULL, whenField = ServiceMetaModelDto.BEAN_NAME, is = NOT_NULL)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Builder.Default
    List<@Valid AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
