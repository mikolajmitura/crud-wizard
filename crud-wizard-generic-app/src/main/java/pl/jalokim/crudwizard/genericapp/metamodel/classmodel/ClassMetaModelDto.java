package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldShouldWhenOther(field = "name", should = NOT_NULL, whenField = "className", is = NULL)
@FieldShouldWhenOther(field = "name", should = NULL, whenField = "className", is = NOT_NULL)
@FieldShouldWhenOther(field = "className", should = NULL, whenField = "name", is = NOT_NULL)
@FieldShouldWhenOther(field = "fields", should = NULL, whenField = "className", is = NOT_NULL)
@FieldShouldWhenOther(field = "fields", should = NOT_EMPTY, whenField = "name", is = NOT_NULL)
@FieldShouldWhenOther(field = "genericTypes", should = NULL, whenField = "name", is = NOT_NULL)
@FieldShouldWhenOther(field = "extendsFromModels", should = NULL, whenField = "className", is = NOT_NULL)
public class ClassMetaModelDto extends AdditionalPropertyMetaModelDto {

    @NotNull(groups = UpdateContext.class)
    Long id;

    String name;

    String className;

    List<@Valid ClassMetaModelDto> genericTypes;

    List<@Valid FieldMetaModelDto> fields;

    List<@Valid ValidatorMetaModelDto> validators;

    List<@Valid ClassMetaModelDto> extendsFromModels;

}
