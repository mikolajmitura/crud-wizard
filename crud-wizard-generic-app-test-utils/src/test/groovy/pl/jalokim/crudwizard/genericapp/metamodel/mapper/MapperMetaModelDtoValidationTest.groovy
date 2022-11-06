package pl.jalokim.crudwizard.genericapp.metamodel.mapper

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
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.exception.handler.SimpleDummyDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMetaModelValidationTestSpec
import pl.jalokim.crudwizard.genericapp.metamodel.ScriptLanguage
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByDeclaredFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.utils.collection.Elements
import spock.lang.Unroll

class MapperMetaModelDtoValidationTest extends BaseMetaModelValidationTestSpec {

    public static final String MAPPER_BY_SCRIPT = "mapper-by-script"

    @Unroll
    def "return expected validation messages for given mapperMetaModelDto: #mapperMetaModelDto"() {
        given:
        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(123L, new MetaModelContext(),
            EndpointMetaModelDto.builder().build())
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
        mapperMetaModelDto                               | expectedErrors
        createValidScriptMapper()                        | []

        createValidScriptMapper().toBuilder()
            .mapperName(null)
            .mapperScript(null)
            .build()                                     | [
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
            .build()                                     | [
            errorEntry("mapperBeanAndMethod",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NULL))),
            errorEntry("mapperGenerateConfiguration",
                whenFieldIsInStateThenOthersShould("mapperType", EQUAL_TO_ANY,
                    ["SCRIPT"], fieldShouldWithoutWhenMessage(NULL)))
        ]

        createValidByBeanMapper()                        | []

        createValidByBeanMapper().toBuilder()
            .mapperName(randomText())
            .build()                                     | []

        createValidByBeanMapper().toBuilder()
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .beanName(null)
                .className(null)
                .methodName(null)
                .build())
            .build()                                     | [
            errorEntry("mapperBeanAndMethod.className", notNullMessage()),
            errorEntry("mapperBeanAndMethod.methodName", notNullMessage()),
        ]

        createValidByBeanMapper().toBuilder()
            .mapperScript(createMapperScript())
            .mapperGenerateConfiguration(createGenerateConfigurationDto("someName"))
            .build()                                     | [
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

        createValidByClassMapper()                       | []

        createValidGeneratedMapper()                     | []

        createValidGeneratedMapper().toBuilder()
            .mapperName(null)
            .mapperGenerateConfiguration(null)
            .build()                                     | [
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
            .build()                                     | [
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
            .build()                                     | [
            errorEntry("name",
                createMessagePlaceholder("UniqueMapperNamesValidator.root.names.should.be.the.same",
                    wrapAsExternalPlaceholder("mapperGenerateConfiguration.rootConfiguration.name"))
                    .translateMessage()),
            errorEntry("mapperGenerateConfiguration.subMappersAsMethods[0]",
                getMessage("UniqueMapperNamesValidator.not.unique.method.name")),
            errorEntry("mapperGenerateConfiguration.subMappersAsMethods[2]",
                getMessage("UniqueMapperNamesValidator.not.unique.method.name")),
        ]

        createInvalidConfigWithProblemsInGeneratedCode() | [
            errorEntry("",
                createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
                    SimpleDummyDto.canonicalName, String.canonicalName, "hash", "")
                    .translateMessage()),
            errorEntry("",
                createMessagePlaceholder("mapper.not.found.assign.strategy",
                    "createdBy", "targetDocument", "createdBy", "")
                    .translateMessage()),
            errorEntry("",
                createMessagePlaceholder("mapper.not.found.assign.strategy",
                    "serialNumbers", "targetDocument", "serialNumbers",
                    " " + createMessagePlaceholder("mapper.mapping.problem.reason", "{mapper.found.to.many.mappings.for.simple.type}"))
                    .translateMessage()),
        ]

        createInvalidConfigWithCompilationProblem()      | [
            errorEntry("",
                Elements.of("compilation problems: ",
                    "target\\generatedMappers\\123\\pl\\jalokim\\crudwizard\\generated\\mapper\\MsourceDocumentToMtargetDocumentMapper.java:31: " +
                        "error: incompatible types: int cannot be converted to LocalDateTime",
                    "\t\tmap.put(\"generated\", ((java.time.LocalDateTime) 12323));",
                    "\t\t                                                ^",
                    "target\\generatedMappers\\123\\pl\\jalokim\\crudwizard\\generated\\mapper\\MsourceDocumentToMtargetDocumentMapper.java:41: " +
                        "error: incompatible types: int cannot be converted to LocalDateTime",
                    "\t\tmap.put(\"otherField\", ((java.time.LocalDateTime) 123));",
                    "\t\t                                                 ^").concatWithNewLines())
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

    private static MapperMetaModelDto createInvalidConfigWithProblemsInGeneratedCode(String mapperName = randomText()) {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.GENERATED)
            .mapperName(mapperName)
            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                .fieldMetaResolverForRawTarget(
                    FieldMetaResolverConfigurationDto.builder()
                        .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                        .build()
                )
                .rootConfiguration(MapperConfigurationDto.builder()
                    .name(mapperName)
                    .targetMetaModel(ClassMetaModelDto.builder()
                        .name("targetDocument")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("numberId", Long, []),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("generated", LocalDateTime),
                            createValidFieldMetaModelDto("hash", String),
                            createValidFieldMetaModelDto("serialNumbers", String),
                            createValidFieldMetaModelDto("createdBy", String),
                            createValidFieldMetaModelDto("modifiedBy", String),
                        ])
                        .build())
                    .sourceMetaModel(ClassMetaModelDto.builder()
                        .name("sourceDocument")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("numberId", Long, []),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("generated", LocalDateTime),
                            createValidFieldMetaModelDto("hash", SimpleDummyDto),
                            createValidFieldMetaModelDto("serialNumber1", String),
                            createValidFieldMetaModelDto("serialNumber2", String),
                        ])
                        .build())
                    .propertyOverriddenMapping([
                        PropertiesOverriddenMappingDto.builder()
                            .targetAssignPath("serialNumbers")
                            .sourceAssignExpression("serialNumber1")
                            .build(),
                        PropertiesOverriddenMappingDto.builder()
                            .targetAssignPath("serialNumbers")
                            .sourceAssignExpression("serialNumber2")
                            .build(),
                        PropertiesOverriddenMappingDto.builder()
                            .targetAssignPath("modifiedBy")
                            .ignoreField(true)
                            .build(),
                    ])
                    .build())
                .build())
            .build()
    }

    private static MapperMetaModelDto createInvalidConfigWithCompilationProblem(String mapperName = randomText()) {
        MapperMetaModelDto.builder()
            .mapperType(MapperType.GENERATED)
            .mapperName(mapperName)
            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                .fieldMetaResolverForRawTarget(
                    FieldMetaResolverConfigurationDto.builder()
                        .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                        .build()
                )
                .rootConfiguration(MapperConfigurationDto.builder()
                    .name(mapperName)
                    .targetMetaModel(ClassMetaModelDto.builder()
                        .name("targetDocument")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("numberId", Long, []),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("generated", LocalDateTime),
                            createValidFieldMetaModelDto("otherField", LocalDateTime),
                        ])
                        .build())
                    .sourceMetaModel(ClassMetaModelDto.builder()
                        .name("sourceDocument")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("numberId", Long, []),
                            createValidFieldMetaModelDto("name", String)
                        ])
                        .build())
                    .propertyOverriddenMapping([
                        PropertiesOverriddenMappingDto.builder()
                            .targetAssignPath("generated")
                            .sourceAssignExpression("j(12323)")
                            .build(),
                        PropertiesOverriddenMappingDto.builder()
                            .targetAssignPath("otherField")
                            .sourceAssignExpression("j(123)")
                            .build(),
                    ])
                    .build())
                .build())
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
                .sourceMetaModel(createValidClassMetaModel().toBuilder()
                    .name("otherPerson")
                    .build())
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
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("motherData.motherData")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("motherData.fatherData")
                        .ignoreField(true)
                        .build(),
                ])
                .build())
            .build()
    }

    private static ClassMetaModelDto createValidClassMetaModel() {
        simplePersonClassMetaModel()
    }

    static ClassMetaModelDto simplePersonClassMetaModel() {
        ClassMetaModelDto.builder()
            .name("simplePerson")
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
