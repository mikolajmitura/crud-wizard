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
@FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NULL)
@FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ClassMetaModelDto.CLASS_NAME, should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ClassMetaModelDto.FIELDS, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = "fields", should = NOT_EMPTY, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = "genericTypes", should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = "extendsFromModels", should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL)
public class ClassMetaModelDto extends AdditionalPropertyMetaModelDto {

    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";
    public static final String FIELDS = "fields";

    @NotNull(groups = UpdateContext.class)
    Long id;

    String name;

    String className;

    List<@Valid ClassMetaModelDto> genericTypes;

    List<@Valid FieldMetaModelDto> fields;

    List<@Valid ValidatorMetaModelDto> validators;

    List<@Valid ClassMetaModelDto> extendsFromModels;

}
