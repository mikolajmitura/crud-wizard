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
@FieldShouldWhenOther(field = "className", should = NULL, whenField = "mapperScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "beanName", should = NULL, whenField = "mapperScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "methodName", should = NULL, whenField = "mapperScript", is = NOT_NULL)
@FieldShouldWhenOther(field = "mapperScript", should = NULL, whenField = "className", is = NOT_NULL)
@FieldShouldWhenOther(field = "mapperScript", should = NULL, whenField = "methodName", is = NOT_NULL)
@FieldShouldWhenOther(field = "mapperScript", should = NULL, whenField = "beanName", is = NOT_NULL)
public class MapperMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    String className;
    String beanName;
    String methodName;
    String mapperScript;
    @NotNull
    MappingDirection mappingDirection;
}
