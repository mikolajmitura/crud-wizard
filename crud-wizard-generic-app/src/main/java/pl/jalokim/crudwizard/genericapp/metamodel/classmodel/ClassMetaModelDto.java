package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.EnumValuesInAdditionalProperties;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@WhenFieldIsInStateThenOthersShould(whenField = "id", is = NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NULL),
    @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL),
    @FieldShouldWhenOther(field = ClassMetaModelDto.CLASS_NAME, should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
})

@FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL,
    whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE)
@FieldShouldWhenOther(field = ClassMetaModelDto.FIELDS, should = NULL,
    whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE)
@FieldShouldWhenOther(field = ClassMetaModelDto.GENERIC_TYPES, should = NULL,
    whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE)
@FieldShouldWhenOther(field = ClassMetaModelDto.EXTENDS_FROM_MODELS, should = NULL,
    whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE)

@FieldShouldWhenOther(field = ClassMetaModelDto.GENERIC_TYPES, should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
@FieldShouldWhenOther(field = ClassMetaModelDto.EXTENDS_FROM_MODELS, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL)
@EnumValuesInAdditionalProperties
public class ClassMetaModelDto extends AdditionalPropertyMetaModelDto {

    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";
    public static final String FIELDS = "fields";
    public static final String GENERIC_TYPES = "genericTypes";
    public static final String EXTENDS_FROM_MODELS = "extendsFromModels";
    public static final String IS_GENERIC_ENUM_TYPE = "isGenericEnumType";
    public static final String TRUE = "true";

    @NotNull(groups = UpdateContext.class)
    Long id;

    String name;

    @ClassExists(canBeAbstractOrInterface = true)
    String className;

    @NotNull
    @Builder.Default
    Boolean isGenericEnumType = false;

    /**
     * Only for read only. This value is set
     */
    Boolean simpleRawClass;

    List<@Valid ClassMetaModelDto> genericTypes;

    List<@Valid FieldMetaModelDto> fields;

    List<@Valid ValidatorMetaModelDto> validators;

    List<@Valid ClassMetaModelDto> extendsFromModels;

    @Builder.Default
    List<@Valid AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
