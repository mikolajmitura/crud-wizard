package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.ID;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto.MAPPER_TYPE;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.metamodels.MapperType;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
@WhenFieldIsInStateThenOthersShould(whenField = ID, is = NULL, thenOthersShould = {
    @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_TYPE, should = NOT_NULL, whenField = ID, is = NULL),
})
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = "BEAN_OR_CLASS_NAME",
    thenOthersShould = {
        @FieldShouldWhenOther(field = MapperMetaModelDto.CLASS_NAME, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "BEAN_OR_CLASS_NAME"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.METHOD_NAME, should = NOT_NULL,
            whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, otherFieldValues = "BEAN_OR_CLASS_NAME"
        ),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "BEAN_OR_CLASS_NAME"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_GENERATE_CONFIGURATION, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "BEAN_OR_CLASS_NAME"),
    })
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = "SCRIPT",
    thenOthersShould = {
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_NAME, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.CLASS_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.METHOD_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.BEAN_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_GENERATE_CONFIGURATION, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "SCRIPT"),
    })
@WhenFieldIsInStateThenOthersShould(whenField = MAPPER_TYPE, is = EQUAL_TO_ANY, fieldValues = "GENERATED",
    thenOthersShould = {
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_GENERATE_CONFIGURATION, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_NAME, should = NOT_NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.CLASS_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.METHOD_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.BEAN_NAME, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
        @FieldShouldWhenOther(field = MapperMetaModelDto.MAPPER_SCRIPT, should = NULL, whenField = MAPPER_TYPE,
            is = EQUAL_TO_ANY, otherFieldValues = "GENERATED"),
    })
public class MapperMetaModelDto extends WithAdditionalPropertiesDto {

    public static final String ID = "id";
    public static final String MAPPER_TYPE = "mapperType";
    public static final String MAPPER_SCRIPT = "mapperScript";
    public static final String MAPPER_NAME = "mapperName";
    public static final String CLASS_NAME = "className";
    public static final String METHOD_NAME = "methodName";
    public static final String BEAN_NAME = "beanName";
    public static final String MAPPER_GENERATE_CONFIGURATION = "mapperGenerateConfiguration";

    Long id;

    @Size(min = 3, max = 100)
    String mapperName;

    @Size(min = 3, max = 250)
    @ClassExists
    String className;

    // TODO #1 #validation validation when added new mapper metamodel
    // - verify that this bean, class, method exists
    // - verify that can method arguments will be resolved correctly, expected annotations and
    //  types see in DelegatedServiceMethodInvoker + GenericMapperArgument or just one some java object (without annotations, so then will be sourceObject to map).
    // - verify that newly added mapperMetaModel does not exists already, then use existing id

    // TODO #1 #validation during validation from dto should be get class metamodel etc,
    //  because class metamodels will not exists already in context the same with mapper metamodel etc...

    @Size(min = 3, max = 100)
    String beanName;

    @Size(min = 3, max = 100)
    String methodName;

    @Valid
    MapperScriptDto mapperScript;

    MapperType mapperType;

    @Valid
    MapperGenerateConfigurationDto mapperGenerateConfiguration;

}
