package pl.jalokim.crudwizard.genericapp.metamodel.mapper

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWithoutWhenMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.whenFieldIsInStateThenOthersShould
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import javax.validation.ValidatorFactory
import org.mapstruct.factory.Mappers
import org.springframework.jdbc.core.JdbcTemplate
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.exception.handler.SimpleDummyDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType
import pl.jalokim.crudwizard.genericapp.metamodel.ScriptLanguage
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.TemporaryContextLoader
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByDeclaredFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class MapperMetaModelDtoValidationTest extends UnitTestSpec {

    public static final String MAPPER_BY_SCRIPT = "mapper-by-script"

    private JdbcTemplate jdbcTemplate = Mock()
    private ClassMetaModelMapper classMetaModelMapper = Mappers.getMapper(ClassMetaModelMapper)
    private FieldMetaModelMapper fieldMetaModelMapper = Mappers.getMapper(FieldMetaModelMapper)
    private MapperMetaModelMapper mapperMetaModelMapper = Mappers.getMapper(MapperMetaModelMapper)
    private jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    private ValidatorFactory validatorFactory = Mock()
    private MetaModelContextService metaModelContextService = Mock()

    private TemporaryContextLoader temporaryContextLoader = new TemporaryContextLoader(validatorFactory,
        metaModelContextService, classMetaModelMapper, mapperMetaModelMapper
    )

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(jdbcTemplate, classMetaModelMapper)

    def setup() {
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", fieldMetaModelMapper)
        setValueForField(fieldMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))
        setValueForField(classMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))
        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> 0

        GenericBeansProvider genericBeanProvider = Mock()
        InstanceLoader instanceLoader = Mock()

        setValueForField(mapperMetaModelMapper, "genericBeanProvider", genericBeanProvider)
        setValueForField(mapperMetaModelMapper, "instanceLoader", instanceLoader)
        setValueForField(mapperMetaModelMapper, "beanMethodMetaModelCreator", new BeanMethodMetaModelCreator(
            new MethodSignatureMetaModelResolver(jsonObjectMapper)))
        setValueForField(mapperMetaModelMapper, "classMetaModelMapper", classMetaModelMapper)
        setValueForField(mapperMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))

    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }

    @Unroll
    def "return expected validation messages for given mapperMetaModelDto: #mapperMetaModelDto"() {
        given:
        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(new MetaModelContext())
        TemporaryModelContextHolder.setTemporaryContext(temporaryMetaModelContext)

        def generateConfig = mapperMetaModelDto.getMapperGenerateConfiguration()
        if (generateConfig) {
            def rootMapperConfiguration = generateConfig.getRootConfiguration()
            if (rootMapperConfiguration) {
                temporaryContextLoader.updateOrCreateClassMetaModelInContext(rootMapperConfiguration.getTargetMetaModel())
                temporaryContextLoader.updateOrCreateClassMetaModelInContext(rootMapperConfiguration.getSourceMetaModel())
            }
            if (generateConfig.getSubMappersAsMethods()) {
                generateConfig.getSubMappersAsMethods().forEach {
                    mapperConfig ->
                        temporaryContextLoader.updateOrCreateClassMetaModelInContext(mapperConfig.getTargetMetaModel())
                        temporaryContextLoader.updateOrCreateClassMetaModelInContext(mapperConfig.getSourceMetaModel())
                }
            }
        }

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(mapperMetaModelDto)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        mapperMetaModelDto           | expectedErrors
        createValidScriptMapper()    | []

        createValidScriptMapper().toBuilder()
            .mapperName(null)
            .mapperScript(null)
            .build()                 | [
            errorEntry("mapperName",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NOT_NULL))),
            errorEntry("mapperScript",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NOT_NULL))),
        ]

        createValidScriptMapper().toBuilder()
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .beanName("dummyService")
                .className(DummyService.canonicalName)
                .methodName("fetchSomeMap")
                .build())
            .mapperGenerateConfiguration(createGenerateConfigurationDto(MAPPER_BY_SCRIPT))
            .build()                 | [
            errorEntry("mapperBeanAndMethod",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NULL))),
            errorEntry("mapperGenerateConfiguration",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NULL)))
        ]

        createValidByBeanMapper()    | []

        createValidByBeanMapper().toBuilder()
            .mapperName(randomText())
            .build()                 | []

        createValidByBeanMapper().toBuilder()
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .beanName(null)
                .className(null)
                .methodName(null)
                .build())
            .build()                 | [
            errorEntry("mapperBeanAndMethod.className", notNullMessage()),
            errorEntry("mapperBeanAndMethod.methodName", notNullMessage()),
        ]

        createValidByBeanMapper().toBuilder()
            .mapperScript(createMapperScript())
            .mapperGenerateConfiguration(createGenerateConfigurationDto("someName"))
            .build()                 | [
            errorEntry("mapperScript",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["BEAN_OR_CLASS_NAME"], fieldShouldWithoutWhenMessage(NULL))),
            errorEntry("mapperGenerateConfiguration",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["BEAN_OR_CLASS_NAME"], fieldShouldWithoutWhenMessage(NULL))),
            errorEntry("name",
                createMessagePlaceholder("UniqueMapperNamesValidator.root.names.should.be.the.same",
                    wrapAsExternalPlaceholder("mapperGenerateConfiguration.rootConfiguration.name"))
                    .translateMessage()),
        ]

        createValidByClassMapper()   | []

        createValidGeneratedMapper() | []

        createValidGeneratedMapper().toBuilder()
            .mapperName(null)
            .mapperGenerateConfiguration(null)
            .build()                 | [
            errorEntry("mapperName",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["GENERATED"], fieldShouldWithoutWhenMessage(NOT_NULL))),
            errorEntry("mapperGenerateConfiguration",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["GENERATED"], fieldShouldWithoutWhenMessage(NOT_NULL))),
        ]

        createValidGeneratedMapper().toBuilder()
            .mapperScript(createMapperScript())
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .beanName("dummyService")
                .className(DummyService.canonicalName)
                .methodName("fetchSomeMap")
                .build())
            .build()                 | [
            errorEntry("mapperBeanAndMethod",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["GENERATED"], fieldShouldWithoutWhenMessage(NULL))),
            errorEntry("mapperScript",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["GENERATED"], fieldShouldWithoutWhenMessage(NULL)))
        ]

        createValidGeneratedMapper("mapperName1").toBuilder()
            .mapperGenerateConfiguration(createGenerateConfigurationDto("mapperName2").toBuilder()
                .subMappersAsMethods([
                    MapperConfigurationDto.builder()
                        .name("mapString")
                        .targetMetaModel(createClassMetaModelDtoFromClass(String))
                        .sourceMetaModel(createClassMetaModelDtoFromClass(Long))
                        .build(),
                    MapperConfigurationDto.builder()
                        .name("mapLongs")
                        .targetMetaModel(createClassMetaModelDtoFromClass(String))
                        .sourceMetaModel(createClassMetaModelDtoFromClass(Long))
                        .build(),
                    MapperConfigurationDto.builder()
                        .name("mapString")
                        .targetMetaModel(createClassMetaModelDtoFromClass(String))
                        .sourceMetaModel(createClassMetaModelDtoFromClass(Integer))
                        .build()
                ])
                .build())
            .build()                 | [
            errorEntry("name",
                createMessagePlaceholder("UniqueMapperNamesValidator.root.names.should.be.the.same",
                    wrapAsExternalPlaceholder("mapperGenerateConfiguration.rootConfiguration.name"))
                    .translateMessage()),
            errorEntry("mapperGenerateConfiguration.subMappersAsMethods[0]",
                getMessage("UniqueMapperNamesValidator.not.unique.method.name")),
            errorEntry("mapperGenerateConfiguration.subMappersAsMethods[2]",
                getMessage("UniqueMapperNamesValidator.not.unique.method.name")),
        ]
    }

    private static MapperMetaModelDto createValidScriptMapper() {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.SCRIPT)
            .mapperName(MAPPER_BY_SCRIPT)
            .mapperScript(createMapperScript())
            .build()
    }

    private static MapperScriptDto createMapperScript() {
        MapperScriptDto.builder()
            .sourceMetaModel(createValidClassMetaModel())
            .targetMetaModel(createValidClassMetaModel())
            .scriptCode("some code")
            .language(ScriptLanguage.GROOVY)
            .build()
    }

    private static MapperMetaModelDto createValidByBeanMapper() {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.BEAN_OR_CLASS_NAME)
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .beanName("dummyService")
                .className(DummyService.canonicalName)
                .methodName("fetchSomeMap")
                .build())
            .build()
    }

    private static MapperMetaModelDto createValidByClassMapper() {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.BEAN_OR_CLASS_NAME)
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .className(SomeMapper.canonicalName)
                .methodName("someMapperMethod")
                .build())
            .build()
    }

    private static MapperMetaModelDto createValidGeneratedMapper(String mapperName = randomText()) {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.GENERATED)
            .mapperName(mapperName)
            .mapperGenerateConfiguration(createGenerateConfigurationDto(mapperName))
            .build()
    }

    private static MapperGenerateConfigurationDto createGenerateConfigurationDto(String rootMapperName) {
        MapperGenerateConfigurationDto.builder()
            .fieldMetaResolverForRawTarget(
                FieldMetaResolverConfigurationDto.builder()
                    .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                    .fieldMetaResolverForClass([
                        FieldMetaResolverForClassEntryDto.builder()
                            .className(SimpleDummyDto.canonicalName)
                            .resolverClassName(ByDeclaredFieldsResolver.canonicalName)
                            .build()
                    ])
                    .build()
            )
            .rootConfiguration(MapperConfigurationDto.builder()
                .name(rootMapperName)
                .targetMetaModel(otherPersonClassMetaModel())
                .sourceMetaModel(createValidClassMetaModel())
                .propertyOverriddenMapping([
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("motherData")
                        .sourceAssignExpression("")
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("otherId")
                        .sourceAssignExpression("id")
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("fatherData")
                        .build()
                ])
                .build())
            .build()
    }

    private static ClassMetaModelDto createValidClassMetaModel() {
        simplePersonClassMetaModel()
    }

    static ClassMetaModelDto simplePersonClassMetaModel() {
        ClassMetaModelDto.builder()
            .name("person")
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("id", Long, []),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("birthDate", LocalDate)
            ])
            .build()
    }

    static ClassMetaModelDto otherPersonClassMetaModel() {
        ClassMetaModelDto.builder()
            .name("person")
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("otherId", Long, []),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("birthDate", LocalDate),
                createValidFieldMetaModelDto("motherData", simplePersonClassMetaModel()),
                createValidFieldMetaModelDto("fatherData", simplePersonClassMetaModel())
            ])
            .build()
    }

    static class SomeMapper {
        Long someMapperMethod(String someObject) {
            0
        }
    }
}
