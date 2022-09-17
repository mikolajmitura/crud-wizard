package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.IdExists;
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.EnumValuesInAdditionalProperties;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ExistFullDefinitionInTempContextByName;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)

@WhenFieldIsInStateThenOthersShould(whenField = "classMetaModelDtoType",
    is = EQUAL_TO_ANY,
    fieldValues = "BY_ID",
    thenOthersShould = {
        @FieldShouldWhenOther(field = ClassMetaModelDto.ID, should = NOT_NULL, whenField = "classMetaModelDtoType", is = NOT_NULL)
    })

@WhenFieldIsInStateThenOthersShould(whenField = "classMetaModelDtoType",
    is = EQUAL_TO_ANY,
    fieldValues = "BY_NAME",
    thenOthersShould = {
        @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL, whenField = "classMetaModelDtoType", is = NOT_NULL)
    })

@WhenFieldIsInStateThenOthersShould(whenField = "classMetaModelDtoType",
    is = EQUAL_TO_ANY,
    fieldValues = "DEFINITION",
    thenOthersShould = {
        @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL,
            whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE),
        @FieldShouldWhenOther(field = ClassMetaModelDto.FIELDS, should = NULL,
            whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE),
        @FieldShouldWhenOther(field = ClassMetaModelDto.GENERIC_TYPES, should = NULL,
            whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE),
        @FieldShouldWhenOther(field = ClassMetaModelDto.EXTENDS_FROM_MODELS, should = NULL,
            whenField = ClassMetaModelDto.IS_GENERIC_ENUM_TYPE, is = EQUAL_TO_ANY, otherFieldValues = ClassMetaModelDto.TRUE),

        @FieldShouldWhenOther(field = ClassMetaModelDto.GENERIC_TYPES, should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL),
        @FieldShouldWhenOther(field = ClassMetaModelDto.EXTENDS_FROM_MODELS, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL)
    })

@WhenFieldIsInStateThenOthersShould(whenField = "id", is = NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NOT_NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NULL),
    @FieldShouldWhenOther(field = ClassMetaModelDto.NAME, should = NULL, whenField = ClassMetaModelDto.CLASS_NAME, is = NOT_NULL),
    @FieldShouldWhenOther(field = ClassMetaModelDto.CLASS_NAME, should = NULL, whenField = ClassMetaModelDto.NAME, is = NOT_NULL)
})
@EnumValuesInAdditionalProperties
@ExistFullDefinitionInTempContextByName
public class ClassMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";
    public static final String FIELDS = "fields";
    public static final String GENERIC_TYPES = "genericTypes";
    public static final String EXTENDS_FROM_MODELS = "extendsFromModels";
    public static final String IS_GENERIC_ENUM_TYPE = "isGenericEnumType";
    public static final String TRUE = "true";

    @NotNull(groups = UpdateContext.class)
    @IdExists(entityClass = ClassMetaModelEntity.class, groups = FirstValidationPhase.class)
    Long id;

    @UniqueValue(entityClass = ClassMetaModelEntity.class)
    // TODO uniqueness should be checked in whole temp context, due to fact that in one flow somebody can provide the same names
    @Size(min = 3, max = 100, groups = FirstValidationPhase.class)
    String name;

    @ClassExists(canBeAbstractOrInterface = true, groups = FirstValidationPhase.class)
    @Size(min = 3, max = 250)
    String className;

    /**
     * From this class is created metamodel, with fields and then it became generic meta model.
     */
    @Size(min = 3, max = 250)
    String basedOnClass;

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

    @NotNull(groups = FirstValidationPhase.class)
    @Builder.Default
    MetaModelDtoType classMetaModelDtoType = MetaModelDtoType.DEFINITION;

    public static ClassMetaModelDto buildClassMetaModelDtoWithId(Long id) {
        return ClassMetaModelDto.builder()
            .id(id)
            .classMetaModelDtoType(MetaModelDtoType.BY_ID)
            .build();
    }

    public static ClassMetaModelDto buildClassMetaModelDtoWithName(String name) {
        return ClassMetaModelDto.builder()
            .name(name)
            .classMetaModelDtoType(MetaModelDtoType.BY_NAME)
            .build();
    }

    @JsonIgnore
    public boolean isFullDefinitionType() {
        return MetaModelDtoType.DEFINITION.equals(classMetaModelDtoType);
    }
}
