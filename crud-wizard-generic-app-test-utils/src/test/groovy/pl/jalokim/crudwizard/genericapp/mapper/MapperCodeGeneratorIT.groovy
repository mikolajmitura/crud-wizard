package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.makeLineEndingAsUnix
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression.createRawJavaCodeExpression

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.ClassHasSamplePersonDto
import pl.jalokim.crudwizard.core.sample.ForTestMappingMultiSourceDto
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSimpleSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement
import pl.jalokim.crudwizard.genericapp.mapper.conversion.MappingCollections
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeContact1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1Entity
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomePerson1
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.utils.file.FileUtils
import pl.jalokim.utils.template.TemplateAsText
import spock.lang.Unroll

class MapperCodeGeneratorIT extends GenericAppWithReloadMetaContextSpecification {

    public static final MapperGenerateConfiguration EMPTY_CONFIG = MapperGenerateConfiguration.builder()
        .rootConfiguration(MapperConfiguration.builder().build())
        .build()

    @Autowired
    MapperCodeGenerator mapperGenerator

    @Unroll
    def "return expected code for #expectedFileName"() {
        given:
        def mergedMapperConfiguration = mapperGenerateConfiguration.rootConfiguration.toBuilder()
            .sourceMetaModel(sourceMetaModel)
            .targetMetaModel(targetMetaModel)
            .build()

        def newMapperGenerateConfiguration = mapperGenerateConfiguration.toBuilder()
            .rootConfiguration(mergedMapperConfiguration)
            .build()

        mapperGenerateConfiguration.mapperConfigurationByMethodName.each {methodName, methodMapperConfig ->
            newMapperGenerateConfiguration.addSubMapperConfiguration(methodName, methodMapperConfig)
        }

        when:
        def result = mapperGenerator.generateMapperCodeMetadata(newMapperGenerateConfiguration)

        then:
        def folderPath = "target/generated-test-sources/mappers/pl/jalokim/crudwizard/generated/mapper"
        FileUtils.createDirectories(folderPath)
        def mapperClassName = String.format("%sTo%sMapper",
            getClassModelInfoForGeneratedCode(sourceMetaModel),
            getClassModelInfoForGeneratedCode(targetMetaModel)
        )
        FileUtils.writeToFile(String.format("%s/%s.java", folderPath, mapperClassName), result)
        makeLineEndingAsUnix(result) == makeLineEndingAsUnix(TemplateAsText.fromClassPath("expectedCode/" + expectedFileName).currentTemplateText)

        where:
        sourceMetaModel                    | targetMetaModel                               | mapperGenerateConfiguration                                    |
            expectedFileName

        modelFromClass(Long)               | modelFromClass(Long)                          | EMPTY_CONFIG                                                   |
            "simple_Long_to_Long"

        modelFromClass(Long)               | modelFromClass(String)                        | EMPTY_CONFIG                                                   |
            "simple_Long_to_String"

        modelFromClass(SamplePersonDto)    | getPersonMetaModel()                          | EMPTY_CONFIG                                                   |
            "class_SamplePersonDto_to_model_person"

        // mapping from map to Dto via builder
        getPersonMetaModel()               | modelFromClass(SamplePersonDto)               | EMPTY_CONFIG                                                   |
            "model_person_to_class_SamplePersonDto"

        // mapping from map to Dto via builder, should get fields only from SomeDtoWithBuilder,
        // not from upper class due to @Builder only on SomeDtoWithBuilder classs
        getSomeDtoWithBuilderModel()       | modelFromClass(SomeDtoWithBuilder)            | EMPTY_CONFIG                                                   |
            "model_someDtoWithBuilder_to_class_SomeDtoWithBuilder"

        // mapping from map to Dto via builder, should get fields from whole @SuperBuilder hierarchy
        getSomeDtoWithSuperBuilderModel()  | modelFromClass(SomeDtoWithSimpleSuperBuilder) | EMPTY_CONFIG                                                   |
            "model_someDtoWithSuperBuilderModel_to_class_SomeDtoWithSuperBuilder"

        // mapping from map to simple Dto via all args
        getSomeSimpleValueDtoModel()       | modelFromClass(SomeSimpleValueDto)            | EMPTY_CONFIG                                                   |
            "model_SomeSimpleValueDtoModel_to_class_SomeSimpleValueDto"

        // mapping from map to simple Dto via setters
        getSomeDtoWithSettersModel()       | modelFromClass(SomeDtoWithSetters)            | EMPTY_CONFIG                                                   |
            "model_SomeDtoWithSettersModel_to_class_SomeDtoWithSetters"

        // mapping with usage of genericObjectsConversionService and conversionService
        getClassHasSamplePersonModel1()    | modelFromClass(ClassHasSamplePersonDto)       |
            withMapperConfigurations(MapperConfiguration.builder()
                .propertyOverriddenMapping(PropertiesOverriddenMapping.builder().ignoredFields(["someObjectWithFewObjects"]).build())
                .build()
            )                                                                                                                                               |
            "model_ClassHasSamplePersonModel_to_class_ClassHasSamplePersonDto"

        //  mapping from map to Dto with nested methods and should use method when inner conversion is from person2 to SamplePersonDto in few fields
        //  person2 (metamodel) samplePersonDto -> SamplePersonDto samplePersonDto
        //  person2 (metamodel) otherPersonDto -> SamplePersonDto otherPersonDto
        //  used ignoredFields
        //  override field by get properties and by spring bean
        getClassHasSamplePersonModel2()    | modelFromClass(ClassHasSamplePersonDto)       | withMapperConfigurations(ignoredFieldsSamplePersonDtoConfig()) |
            "model_ClassHasSamplePersonModel2_to_class_ClassHasSamplePersonDto"

        // mappings when exists few source:

        // firstPerson = someObject.personPart1
        // firstPerson = someObject.personPart2
        // secondPerson =
        // secondPerson = otherPersonPart2
        // secondPerson = otherPersonPart3

        // /* document.documentData = documentDataPart1.documentData */
        // /* document.documentData = documentDataPart2.documentData */
        // docHolder.document = documentDataPart1
        // docHolder.document = documentDataPart2
        // docHolder.document.id = documentDataPart2.mainDocId
        // sameMappingLikeDocHolder.document = documentDataPart1
        // sameMappingLikeDocHolder.document = documentDataPart2
        // sameMappingLikeDocHolder.document.id = documentDataPart2.mainDocId
        // otherMappingForDocHolder.document = documentDataPart1
        // otherMappingForDocHolder.document = documentDataPart2
        // otherMappingForDocHolder.document.id = @fromLocalDateToStringMapper documentDataPart1.localDateTime66

        multiSourceExampleModel()          | modelFromClass(ForTestMappingMultiSourceDto)  | withMapperConfigurations(multiSourceConfig())                  |
            "model_multiSourceExampleModel_to_class_ForTestMappingMultiSourceDto"

        // usage of nested configured method for mappings
        SOME_PERSON_MODEL1                 | modelFromClass(SomePerson1)                   | MAPPING_PERSON_1_CONFIG                                        |
            "mapping_person1_model_to_class"

        // usage of nested configured method for mappings (from class to model) and globalIgnoreMappingProblems during map to target
        modelFromClass(SomePerson1)        | SOME_PERSON_MODEL1                            | EMPTY_CONFIG.toBuilder()
            .globalIgnoreMappingProblems(true)
            .build()                                                                                                                                        |
            "mapping_person1_class_to_model_globalIgnoreMappingProblems"

        // usage of nested configured method for mappings (from class to model)
        //  ignoreMappingProblem via mapper configuration
        //  ignoreMappingProblem via override property
        modelFromClass(SomePerson1)        | SOME_PERSON_MODEL1                            | SOME_PERSON_MODEL1_FEW_IGNORED                                 |
            "mapping_person1_class_to_model_fewIgnored"

        // should generate method for map inner object instead of use provided nested mapper method.
        modelFromClass(SomePerson1)        | SOME_PERSON_MODEL1                            | withMapperConfigurations(
            MapperConfiguration.builder()
                .propertyOverriddenMapping(
                    PropertiesOverriddenMapping.builder()
                        .ignoredFields(["someMetaData"])
                        .mappingsByPropertyName([
                            "idCard": PropertiesOverriddenMapping.builder()
                                .mappingsByPropertyName([
                                    "validTo": PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([
                                            new BySpringBeanMethodAssignExpression(DummyService,
                                                "dummyService",
                                                "getSomeRandomLocalDate", [])
                                        ])
                                        .build()
                                ])
                                .build()
                        ])
                        .build()
                )
                .build(),
            MapperConfiguration.builder()
                .name("mapDocument")
                .sourceMetaModel(modelFromClass(SomeDocument1))
                .targetMetaModel(SOME_DOCUMENT_MODEL1)
                .propertyOverriddenMapping(
                    PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName(validTo:
                            PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([createFieldsChainExpression(modelFromClass(SomeDocument1), "validToDate")])
                                .build())
                        .build()
                )
                .build()
        )                                                                                                                                                   |
            "generate_method_instead_use_provided"

        // mapping collections elements
        modelFromClass(MappingCollections) | MAPPING_COLLECTIONS_MODEL                     | EMPTY_CONFIG                                                   |
            "mappingCollection_from_model_to_class"

        // mapping collections elements in the opposite way
        MAPPING_COLLECTIONS_MODEL          | modelFromClass(MappingCollections)            | EMPTY_CONFIG                                                   |
            "mappingCollection_from_class_to_model"

        // mapping collections from one element to list via override properties
        // mapping collections from one element to array via override properties
        // mapping collections from one element to set via override properties

        // mapping collections from one element, from other list, from other set, from next one element to set via override properties
        // mapping collections elements by provided mapping method for each element in config
        // mapping collections elements by provided mapping method for whole elements (with the same value) in config
        MAPPING_COLLECTIONS_MODEL          | modelFromClass(MappingCollections)            | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    "listList"  :
                        PropertiesOverriddenMapping.builder()
                            .valueMappingStrategy([
                                createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "someOneField1")
                            ])
                            .build(),
                    "arrayList" : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "someOneField1")
                        ])
                        .build(),
                    "setSet"    : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "someOneField2")
                        ])
                        .build(),
                    "arrayList2": PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "someOneField1"),
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "arrayList"),
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "arraySet"),
                            createFieldsChainExpression(MAPPING_COLLECTIONS_MODEL, "someOneField2"),
                        ])
                        .build(),
                    "arraySet"  : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new BySpringBeanMethodAssignExpression(NormalSpringService,
                                "normalSpringService",
                                "getCollectionElementArray", [
                                new BySpringBeanMethodAssignExpression(NormalSpringService,
                                    "normalSpringService",
                                    "getSomeString", []),
                                new BySpringBeanMethodAssignExpression(DummyService,
                                    "dummyService",
                                    "getSomeRandomText", []),
                            ])
                        ])
                        .build(),
                ])
                .build())
            .build(),
            MapperConfiguration.builder()
                .name("mapCollectionElement")
                .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
                .targetMetaModel(modelFromClass(CollectionElement))
                .build(),
        )                                                                                                                                                   |
            "custom_mappingCollection_from_model_to_class"

        // convert java class to some java class
        modelFromClass(SomeDocument1)      | modelFromClass(SomeDocument1Entity)           | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    someLocalDate: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([createFieldsChainExpression(modelFromClass(SomeDocument1), "validToDate")])
                        .build()
                ])
                .build())
            .build())                                                                                                                                       |
            "convert_java_class_to_some_java_class"

        // TODO #1 test for mapping from enum to metamodel of enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from enum to enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from metamodel of enum to enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from string to metamodel of enum
        // TODO #1 test for mapping from metamodel of enum to string
        // TODO #1 test for mapping from some metamodel to some Dto with map inside (SomeDtoWithSuperBuilder has in hierarchy)
    }

    // TODO #1 test for not found mapping way
    // TODO #1 test for found to many mappers for simple field
    // TODO #1 test for cannot find conversion way
    // TODO #1 test for marked few ignoreMappingProblem during map to target, in some classes, but finally will return exception

    static MapperGenerateConfiguration withMapperConfigurations(MapperConfiguration rootConfiguration, MapperConfiguration... subMappersConfiguration) {
        def mapperGenerateConfiguration = EMPTY_CONFIG.toBuilder()
            .rootConfiguration(rootConfiguration)
            .build()

        subMappersConfiguration.each {
            mapperGenerateConfiguration.addSubMapperConfiguration(it.name, it)
        }

        mapperGenerateConfiguration
    }

    private static ClassMetaModel getSomeDtoWithBuilderModel() {
        ClassMetaModel.builder()
            .name("someDtoWithBuilder")
            .fields([
                createValidFieldMetaModel("test1", String),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("testLong1", Long),
                createValidFieldMetaModel("someId", Long),
                createValidFieldMetaModel("localDateTime1", LocalDateTime),
            ])
            .build()
    }

    private static ClassMetaModel getPersonMetaModel() {
        ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("id", Long),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("birthDay", LocalDate),
                createValidFieldMetaModel("lastLogin", LocalDateTime),
            ])
            .build()
    }


    private static ClassMetaModel modelFromClass(Class<?> someClass,
        FieldMetaResolverConfiguration fieldMetaResolverConfiguration = WRITE_FIELD_RESOLVER_CONFIG) {
        ClassMetaModelFactory.createNotGenericClassMetaModel(createClassMetaModelFromClass(someClass), fieldMetaResolverConfiguration)
    }

    private static ClassMetaModel getSomeDtoWithSuperBuilderModel() {
        ClassMetaModel.builder()
            .name("someDtoWithSuperBuilderModel")
            .fields([
                createValidFieldMetaModel("someString1", String)
            ])
            .extendsFromModels([
                ClassMetaModel.builder()
                    .name("superDtoWithSuperBuilderModel")
                    .fields([
                        createValidFieldMetaModel("someLong1", Long),
                        createValidFieldMetaModel("superStringField", String),
                        createValidFieldMetaModel("localDateTime1", LocalDateTime),
                    ])
                    .build()
            ])
            .build()
    }

    private static ClassMetaModel getSomeSimpleValueDtoModel() {
        ClassMetaModel.builder()
            .name("someSimpleValueDtoModel")
            .fields([
                createValidFieldMetaModel("someLong3", Long),
                createValidFieldMetaModel("someString3", String),
                createValidFieldMetaModel("someDataTime3", LocalDateTime),
            ])
            .build()
    }

    private static ClassMetaModel getSomeDtoWithSettersModel() {
        ClassMetaModel.builder()
            .name("someDtoWithSettersMode")
            .fields([
                createValidFieldMetaModel("someString2", String),
                createValidFieldMetaModel("someLong2", Long),
                createValidFieldMetaModel("id", Integer),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("birthDay", LocalDateTime),
                createValidFieldMetaModel("lastLogin", LocalDateTime),
            ])
            .build()
    }

    private static ClassMetaModel getClassHasSamplePersonModel1() {
        def personModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("personId", Long),
                createValidFieldMetaModel("personName", String),
            ])
            .build()

        ClassMetaModel.builder()
            .name("classHasSamplePersonModel")
            .fields([
                createValidFieldMetaModel("someId", String),
                createValidFieldMetaModel("samplePersonDto", personModel),
                createValidFieldMetaModel("otherPersonDto", personModel)
            ])
            .build()
    }

    private static ClassMetaModel getClassHasSamplePersonModel2() {
        def someObjectWithFewObjectsModel = ClassMetaModel.builder()
            .name("someObjectWithFewObjectsModel")
            .fields([
                createValidFieldMetaModel("someDtoWithBuilder", ClassMetaModel.builder()
                    .name("someDtoWithBuilderModel")
                    .fields([
                        createValidFieldMetaModel("test1", String),
                        createValidFieldMetaModel("someLocalDateTime", String)
                    ])
                    .build()),
                createValidFieldMetaModel("someDtoWithSetters", ClassMetaModel.builder()
                    .name("SomeDtoWithSettersModel")
                    .fields([
                        createValidFieldMetaModel("someString2", String),
                        createValidFieldMetaModel("surname", String),
                        createValidFieldMetaModel("name", String)
                    ])
                    .build())
            ])
            .build()

        def person2Model = ClassMetaModel.builder()
            .name("person2")
            .fields([
                createValidFieldMetaModel("id", Long),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
            ])
            .build()

        ClassMetaModel.builder()
            .name("classHasSamplePersonModel2")
            .fields([
                createValidFieldMetaModel("someId", String),
                createValidFieldMetaModel("samplePersonDto", person2Model),
                createValidFieldMetaModel("otherPersonDto", person2Model),
                createValidFieldMetaModel("someObjectWithFewObjects", someObjectWithFewObjectsModel)
            ])
            .build()
    }

    static MapperConfiguration ignoredFieldsSamplePersonDtoConfig() {
        def classHasSamplePersonModel2 = getClassHasSamplePersonModel2()

        ClassMetaModel someDtoWithBuilderModel = classHasSamplePersonModel2.getFieldByName("someObjectWithFewObjects")
            .getFieldType().getFieldByName("someDtoWithBuilder")
            .getFieldType()

        def samplePersonDtoMapperConfig = PropertiesOverriddenMapping.builder()
            .ignoredFields(["birthDay", "lastLogin"])
            .build()

        def someDtoWithBuilderConfig = PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName(Map.of(
                "testLong1", PropertiesOverriddenMapping.builder()
                .valueMappingStrategy([
                    new BySpringBeanMethodAssignExpression(
                        NormalSpringService,
                        "normalSpringService",
                        "someMethodName",
                        [new BySpringBeanMethodAssignExpression(NormalSpringService,
                            "normalSpringService",
                            "getSomeString", []),
                         new NullAssignExpression(createClassMetaModelFromClass(Long))]
                    )
                ])
                .build(),
                'localDateTime1', PropertiesOverriddenMapping.builder()
                .valueMappingStrategy([
                    new FieldsChainToAssignExpression(someDtoWithBuilderModel,
                        "sourceObject",
                        [someDtoWithBuilderModel.getFieldByName("someLocalDateTime")]
                    )
                ])
                .build()
            ))
            .build()

        def otherPersonDtoFieldModel = classHasSamplePersonModel2.getFieldByName("otherPersonDto")
        def idOfOtherPersonDtoFieldModel = otherPersonDtoFieldModel.getFieldType().getFieldByName("id")

        def someDtoWithSettersConfig = PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName([
                someLong2: PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([
                        new FieldsChainToAssignExpression(classHasSamplePersonModel2,
                            "rootSourceObject",
                            [otherPersonDtoFieldModel, idOfOtherPersonDtoFieldModel]
                        )
                    ])
                    .build()
            ])
            .ignoredFields(["id", "birthDay", "lastLogin"])
            .build()

        def someObjectWithFewObjectsConfig = PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName(Map.of(
                "someDtoWithBuilder", someDtoWithBuilderConfig,
                "someDtoWithSetters", someDtoWithSettersConfig,
            ))
            .build()

        MapperConfiguration.builder()
            .propertyOverriddenMapping(
                PropertiesOverriddenMapping.builder()
                    .mappingsByPropertyName(Map.of(
                        "samplePersonDto", samplePersonDtoMapperConfig,
                        "otherPersonDto", samplePersonDtoMapperConfig,
                        "someObjectWithFewObjects", someObjectWithFewObjectsConfig
                    ))
                    .build()
            )
            .build()
    }

    private static ClassMetaModel multiSourceExampleModel() {
        def personPart1 = ClassMetaModel.builder()
            .name("personPart1Model")
            .fields([
                createValidFieldMetaModel("someString", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("name", String),
            ])
            .build()

        def personPart2 = ClassMetaModel.builder()
            .name("personPart2Model")
            .fields([
                createValidFieldMetaModel("id", String),
                createValidFieldMetaModel("lastLogin", LocalDateTime),
                createValidFieldMetaModel("birthDay", LocalDate),
                createValidFieldMetaModel("someOtherDto", ClassMetaModel.builder()
                    .name("someOtherDtoModel")
                    .fields([
                        createValidFieldMetaModel("someString3", String),
                        createValidFieldMetaModel("someLong3", Long),
                        createValidFieldMetaModel("someDataTime3", LocalDateTime)
                    ])
                    .build()),
            ])
            .build()

        def someObjectModel = ClassMetaModel.builder()
            .name("someObjectModel")
            .fields([
                createValidFieldMetaModel("personPart1", personPart1),
                createValidFieldMetaModel("personPart2", personPart2),
            ])
            .build()

        ClassMetaModel.builder()
            .name("multiSourceExampleModel")
            .fields([
                createValidFieldMetaModel("someObject", someObjectModel),
                createValidFieldMetaModel("otherPersonPart2", personPart2),
                createValidFieldMetaModel("otherPersonPart3", ClassMetaModel.builder()
                    .name("otherPersonPart3Model")
                    .fields([
                        createValidFieldMetaModel("someString", String),
                        createValidFieldMetaModel("otherString", String),
                        createValidFieldMetaModel("field3", Long),
                    ])
                    .build()),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("documentDataPart1", ClassMetaModel.builder()
                    .name("documentDataPart1Model")
                    .fields([
                        createValidFieldMetaModel("documentData", ClassMetaModel.builder()
                            .name("documentDataP1Model")
                            .fields([
                                createValidFieldMetaModel("serialNumber", String),
                                createValidFieldMetaModel("signedBy", String),
                                createValidFieldMetaModel("field66", Long),
                            ])
                            .build()),
                        createValidFieldMetaModel("otherString33", String),
                        createValidFieldMetaModel("localDateTime66", LocalDateTime),
                    ])
                    .build()),
                createValidFieldMetaModel("documentDataPart2", ClassMetaModel.builder()
                    .name("documentDataP1Model")
                    .fields([
                        createValidFieldMetaModel("mainDocId", Long.class),
                        createValidFieldMetaModel("documentData", ClassMetaModel.builder()
                            .name("documentDataP1Model")
                            .fields([
                                createValidFieldMetaModel("otherString", String),
                                createValidFieldMetaModel("docHash", String),
                                createValidFieldMetaModel("docId", Long),
                            ])
                            .build()),
                    ])
                    .build())
            ])
            .build()
    }

    static MapperConfiguration multiSourceConfig() {
        def multiSourceExampleModel = multiSourceExampleModel()
        def someObjectField = multiSourceExampleModel.getFieldByName("someObject")
        def someObjectModel = someObjectField.getFieldType()

        def documentDataPart1Expression = new FieldsChainToAssignExpression(multiSourceExampleModel,
            "rootSourceObject", [multiSourceExampleModel.getFieldByName("documentDataPart1")])

        def documentDataPart2Expression = new FieldsChainToAssignExpression(multiSourceExampleModel,
            "rootSourceObject", [multiSourceExampleModel.getFieldByName("documentDataPart2")])

        MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    firstPerson             : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new FieldsChainToAssignExpression(multiSourceExampleModel,
                                "rootSourceObject", [someObjectField, someObjectModel.getFieldByName("personPart1")]),
                            new FieldsChainToAssignExpression(multiSourceExampleModel,
                                "rootSourceObject", [someObjectField, someObjectModel.getFieldByName("personPart2")])
                        ])
                        .build(),
                    secondPerson            : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createRawJavaCodeExpression(multiSourceExampleModel, "sourceObject"),
                            new FieldsChainToAssignExpression(multiSourceExampleModel,
                                "rootSourceObject", [multiSourceExampleModel.getFieldByName("otherPersonPart2")]),
                            new FieldsChainToAssignExpression(multiSourceExampleModel,
                                "rootSourceObject", [multiSourceExampleModel.getFieldByName("otherPersonPart3")]),
                        ])
                        .build(),
                    docHolder               : PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            document: PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    documentDataPart1Expression,
                                    documentDataPart2Expression,
                                ])
                                .mappingsByPropertyName([
                                    id: PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([documentDataPart2Expression.createExpressionWithNextField(
                                            "mainDocId", READ_FIELD_RESOLVER_CONFIG)])
                                        .build()
                                ])
                                .build()
                        ])
                        .build(),
                    sameMappingLikeDocHolder: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            document: PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    documentDataPart1Expression,
                                    documentDataPart2Expression,
                                ])
                                .mappingsByPropertyName([
                                    id: PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([documentDataPart2Expression.createExpressionWithNextField(
                                            "mainDocId", READ_FIELD_RESOLVER_CONFIG)])
                                        .build()
                                ])
                                .build()
                        ])
                        .build(),
                    otherMappingForDocHolder: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            document: PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    documentDataPart1Expression,
                                    documentDataPart2Expression,
                                ])
                                .mappingsByPropertyName([
                                    id: PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([new ByMapperNameAssignExpression(
                                            createClassMetaModelFromClass(String),
                                            documentDataPart1Expression.createExpressionWithNextField("localDateTime66", READ_FIELD_RESOLVER_CONFIG),
                                            "fromLocalDateToStringMapper"
                                        )])
                                        .build()
                                ])
                                .build()
                        ])
                        .build()
                ])
                .build())
            .build()
    }

    static ClassMetaModel SOME_DOCUMENT_MODEL1 = ClassMetaModel.builder()
        .name("someDocument1")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("number", Long),
            createValidFieldMetaModel("validTo", LocalDate),
        ])
        .build()

    static ClassMetaModel SOME_CONTACT_MODEL1 = ClassMetaModel.builder()
        .name("someContact1")
        .fields([
            createValidFieldMetaModel("type", Long),
            createValidFieldMetaModel("value", String),
        ])
        .build()

    static ClassMetaModel SOME_METADATA1 = ClassMetaModel.builder()
        .name("someMetaData1")
        .fields([
            createValidFieldMetaModel("key", Long),
            createValidFieldMetaModel("value", String),
        ])
        .build()

    static ClassMetaModel SOME_PERSON_MODEL1 = ClassMetaModel.builder()
        .name("somePerson1")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("name", String),
            createValidFieldMetaModel("surname", String),
            createValidFieldMetaModel("passport", SOME_DOCUMENT_MODEL1),
            createValidFieldMetaModel("idCard", SOME_DOCUMENT_MODEL1),
            createValidFieldMetaModel("phoneContact", SOME_CONTACT_MODEL1),
            createValidFieldMetaModel("emailContact", SOME_CONTACT_MODEL1),
            createValidFieldMetaModel("someMetaData", SOME_METADATA1),
        ])
        .build()

    static MapperGenerateConfiguration MAPPING_PERSON_1_CONFIG = withMapperConfigurations(
        MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    passport: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new MethodInCurrentClassAssignExpression(
                                "mapPassport",
                                [new FieldsChainToAssignExpression(SOME_PERSON_MODEL1, "sourceObject",
                                    SOME_PERSON_MODEL1.getRequiredFieldByName("passport"))],
                                modelFromClass(SomeDocument1))])
                        .build(),
                    idCard  : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new MethodInCurrentClassAssignExpression(
                                "mapIdCard",
                                [new FieldsChainToAssignExpression(SOME_PERSON_MODEL1, "sourceObject",
                                    SOME_PERSON_MODEL1.getRequiredFieldByName("idCard"))],
                                modelFromClass(SomeDocument1))
                        ])
                        .build()
                ])
                .build())
            .build(),
        MapperConfiguration.builder()
            .name("mapContact")
            .sourceMetaModel(SOME_CONTACT_MODEL1)
            .targetMetaModel(modelFromClass(SomeContact1))
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    fromRootValue: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new FieldsChainToAssignExpression(SOME_PERSON_MODEL1, "rootSourceObject",
                                SOME_PERSON_MODEL1.getRequiredFieldByName("name"))
                        ])
                        .build()
                ])
                .build()
            )
            .build(),
        MapperConfiguration.builder()
            .name("mapPassport")
            .sourceMetaModel(SOME_DOCUMENT_MODEL1)
            .targetMetaModel(modelFromClass(SomeDocument1))
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    validToDate    : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new FieldsChainToAssignExpression(SOME_DOCUMENT_MODEL1, "sourceObject",
                                SOME_DOCUMENT_MODEL1.getRequiredFieldByName("validTo"))
                        ])
                        .build(),
                    fromParentField: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([createFieldsChainExpression(SOME_PERSON_MODEL1, "name", "rootSourceObject")])
                        .build()
                ])
                .build()
            )
            .build(),
        MapperConfiguration.builder()
            .name("mapIdCard")
            .sourceMetaModel(SOME_DOCUMENT_MODEL1)
            .targetMetaModel(modelFromClass(SomeDocument1))
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    validToDate    : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new NullAssignExpression(modelFromClass(LocalDate))
                        ])
                        .build(),
                    fromParentField: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new RawJavaCodeAssignExpression(modelFromClass(String), "\"mappedByIdCard\" + \"Method\"")
                        ])
                        .build()
                ])
                .build()
            )
            .build()
    )

    static MapperGenerateConfiguration SOME_PERSON_MODEL1_FEW_IGNORED = withMapperConfigurations(
        MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    someMetaData: PropertiesOverriddenMapping.builder()
                        .ignoreMappingProblem(true)
                        .build()
                ])
                .build()
            )
            .build(),
        MapperConfiguration.builder()
            .ignoreMappingProblems(true)
            .name("mapDocument")
            .sourceMetaModel(modelFromClass(SomeDocument1))
            .targetMetaModel(SOME_DOCUMENT_MODEL1)
            .build()
    )

    static ClassMetaModel NESTED_ELEMENT_OBJECT_MODEL = ClassMetaModel.builder()
        .name("nestedElementObjectModel")
        .fields([
            createValidFieldMetaModel("name", String),
            createValidFieldMetaModel("surname", String),
        ])
        .build()

    static ClassMetaModel COLLECTION_ELEMENT_MODEL = ClassMetaModel.builder()
        .name("collectionElementModel")
        .fields([
            createValidFieldMetaModel("field1", String),
            createValidFieldMetaModel("field2", String),
            createValidFieldMetaModel("field3", Long),
            createValidFieldMetaModel("someObject", NESTED_ELEMENT_OBJECT_MODEL),
        ])
        .build()

    static ClassMetaModel OTHER_ELEMENT_COLLECTION_MODEL = ClassMetaModel.builder()
        .name("otherElementCollectionModel")
        .fields([
            createValidFieldMetaModel("othElemField1", String),
            createValidFieldMetaModel("othElemField2", String),
        ])
        .build()

    static ClassMetaModel MAPPING_COLLECTIONS_MODEL = ClassMetaModel.builder()
        .name("mappingCollectionsModel")
        .fields([
            createValidFieldMetaModel("strings", ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([modelFromClass(String)])
                .build()),
            createValidFieldMetaModel("longs", ClassMetaModel.builder()
                .realClass(String[])
                .genericTypes([modelFromClass(String)])
                .build()),
            createValidFieldMetaModel("arrayList", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("arrayList2", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("arraySet", ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("listList", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("setSet", ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("someMap", ClassMetaModel.builder()
                .realClass(Map)
                .genericTypes([modelFromClass(Long), COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("mappedListElementByProvidedMethod", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([OTHER_ELEMENT_COLLECTION_MODEL])
                .build()),
            createValidFieldMetaModel("someOneField1", COLLECTION_ELEMENT_MODEL),
            createValidFieldMetaModel("someOneField2", COLLECTION_ELEMENT_MODEL),
        ])
        .build()

    private static FieldsChainToAssignExpression createFieldsChainExpression(ClassMetaModel sourceMetaModel,
        String fieldName, String variableName = "sourceObject") {
        new FieldsChainToAssignExpression(sourceMetaModel, variableName, [
            sourceMetaModel.getRequiredFieldByName(fieldName)
        ])
    }
}
