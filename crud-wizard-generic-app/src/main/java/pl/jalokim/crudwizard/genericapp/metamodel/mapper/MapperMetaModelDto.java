package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.BEAN_OR_CLASS_NAME;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.GENERATED;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.ID;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.MAPPER_BEAN_AND_METHOD;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.MAPPER_GENERATE_CONFIGURATION;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.MAPPER_SCRIPT;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.MAPPER_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.SCRIPT;

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
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation.MapperGenerateConfigCheck;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation.UniqueMapperNames;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
@WhenFieldIsInStateThenOthersShould(whenField = ID, is = NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = MAPPER_TYPE, should = NOT_NULL, whenField = ID, is = NULL),
})
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = BEAN_OR_CLASS_NAME,
    thenOthersShould = {
        @FieldShouldWhenOther(field = MAPPER_BEAN_AND_METHOD, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = BEAN_OR_CLASS_NAME),
        @FieldShouldWhenOther(field = MAPPER_SCRIPT, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = BEAN_OR_CLASS_NAME),
        @FieldShouldWhenOther(field = MAPPER_GENERATE_CONFIGURATION, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = BEAN_OR_CLASS_NAME),
    })
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = SCRIPT,
    thenOthersShould = {
        @FieldShouldWhenOther(field = MAPPER_SCRIPT, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = SCRIPT),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_NAME, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = SCRIPT),
        @FieldShouldWhenOther(field = MAPPER_BEAN_AND_METHOD, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = SCRIPT),
        @FieldShouldWhenOther(field = MAPPER_GENERATE_CONFIGURATION, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = SCRIPT),
    })
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = GENERATED,
    thenOthersShould = {
        @FieldShouldWhenOther(field = MAPPER_GENERATE_CONFIGURATION, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = GENERATED),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_NAME, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = GENERATED),
        @FieldShouldWhenOther(field = MAPPER_BEAN_AND_METHOD, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = GENERATED),
        @FieldShouldWhenOther(field = MAPPER_SCRIPT, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = GENERATED),
    })
@UniqueMapperNames
@MapperGenerateConfigCheck
public class MapperMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String ID = "id";
    public static final String MAPPER_TYPE = "mapperType";
    public static final String MAPPER_SCRIPT = "mapperScript";
    public static final String MAPPER_NAME = "mapperName";
    public static final String MAPPER_BEAN_AND_METHOD = "mapperBeanAndMethod";
    public static final String MAPPER_GENERATE_CONFIGURATION = "mapperGenerateConfiguration";
    public static final String GENERATED = "GENERATED";
    public static final String SCRIPT = "SCRIPT";
    public static final String BEAN_OR_CLASS_NAME = "BEAN_OR_CLASS_NAME";

    Long id;

    @Size(min = 3, max = 100)
    @UniqueValue(entityClass = MapperMetaModelEntity.class, entityFieldName = "mapperName")
    // TODO uniqueness should be checked in whole temp context, due to fact that in one flow somebody can provide the same names
    String mapperName;

    @Valid
    BeanAndMethodDto mapperBeanAndMethod;

    @Valid
    MapperScriptDto mapperScript;

    @NotNull(groups = FirstValidationPhase.class)
    @Builder.Default
    MetaModelDtoType metamodelDtoType = MetaModelDtoType.DEFINITION;

    MapperType mapperType;

    @Valid
    MapperGenerateConfigurationDto mapperGenerateConfiguration;

}
