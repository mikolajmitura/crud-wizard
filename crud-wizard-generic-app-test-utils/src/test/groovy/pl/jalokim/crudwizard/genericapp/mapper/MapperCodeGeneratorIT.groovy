package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassModelWithGenerics
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.makeLineEndingAsUnix
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.CLASS_HAS_SAMPLE_PERSON_DTO_AS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.CLASS_HAS_SAMPLE_PERSON_MODEL_2_AS_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.CUSTOM_MAPPING_COLLECTION_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.CUSTOM_MAPPING_COLLECTION_MDL_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.DTO_WITH_BUILDER_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.DTO_WITH_BUILDER_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.DTO_WITH_SUPER_BUILDER_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.DTO_WITH_SUPER_BUILDER_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.EACH_MAP_BY_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.EMPTY_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.ENUM_CONFLICTS_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.EXAMPLE_ENUM_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.FEW_ENUM_MAPPINGS_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.FOR_TEST_MAPPING_MULTI_SOURCE_DTO_AS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.FROM_STRING_TO_OBJECT_CONF_TO_STRING
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.HAS_SAMPLE_PERSON_CLS_1
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.HAS_SAMPLE_PERSON_MDL_1
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.HAS_SAMPLE_PERSON_MODEL_1_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.INSIDE_COLLECTION_ELEMENT
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_AS_LIST_GENERIC_TYPE_1_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_AS_LIST_GENERIC_TYPE_2_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_LIST_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_LIST_MODEL_SAMPLE
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_OF_ELEMENTS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_OF_METAMODELS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_OF_METAMODELS_SAMPLE
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_SET_ELEMENT
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.LIST_SET_ELEMENT_SAMPLE
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MAPPING_COLLECTIONS_AS_CLASS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MAPPING_COLLECTIONS_AS_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MAPPING_COLLECTIONS_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MAPPING_COLLECTION_WITH_STAR_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MAPPING_PERSON_1_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS1
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS1_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS2
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS2_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS3
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS3_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS4
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.METAMODEL_WITH_ENUMS4_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MODEL_WITH_ENUM
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.MULTI_SOURCE_EXAMPLE_MODEL_AS_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OTHER_OBJECT_WITH_ENUM_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OTHER_WITH_ELEMENTS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OTHER_WITH_ELEMENTS_CLS2
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OTHER_WITH_ELEMENTS_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.OTHER_WITH_ELEMENTS_MODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.PERSON_META_MODEL_AS_METAMODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SAMPLE_PERSON_DTO_AS_CLASS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SET_OF_CLASSES
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_CLS_TO_CLS_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_DOC_1_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_DOC_EN_1_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_DTO_WITH_SETTERS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_DTO_WITH_SETTERS_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_ENUM1_METAMODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_ENUM2_METAMODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_ENUM3_METAMODEL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON1_FROM_CLS_MDL_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_1_AS_CLS
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_MODEL1
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_MODEL1_AS_MDL
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_MODEL1_AS_MDL1
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_MODEL1_AS_MDL2
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.SOME_PERSON_MODEL1_FEW_IGNORED
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.STRING_TO_FROM_STRING_TO_OBJECT_CONF
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.createFieldsChainExpression
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.emptyGenericMapperArgument
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.modelFromClass
import static pl.jalokim.crudwizard.genericapp.mapper.MapperCodeGeneratorSamples.withMapperConfigurations
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator.GENERATED_MAPPER_PACKAGE
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.AssignExpressionAsTextResolver.occurredInNotGeneratedMethod
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression.createRawJavaCodeExpression

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.datetime.TimeProvider
import pl.jalokim.crudwizard.core.sample.ClassHasSamplePersonDto
import pl.jalokim.crudwizard.core.sample.ForTestMappingMultiSourceDto
import pl.jalokim.crudwizard.core.sample.FromStringToObject
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSimpleSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.compiler.ClassLoaderService
import pl.jalokim.crudwizard.genericapp.compiler.CodeCompiler
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ExampleEnum2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.MappingCollections
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ObjectForNotFondMappings
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ObjectWithEnum
import pl.jalokim.crudwizard.genericapp.mapper.conversion.OtherWithElements
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1Entity
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomePerson1
import pl.jalokim.crudwizard.genericapp.mapper.dummygenerated.LocalDateToStringMapperDummyGenerated
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.EnumEntriesMapping
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.validation.MapperGenerationException
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.utils.file.FileUtils
import pl.jalokim.utils.template.TemplateAsText
import spock.lang.Unroll

class MapperCodeGeneratorIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    MapperCodeGenerator mapperGenerator

    @Autowired
    CodeCompiler codeCompiler

    @Autowired
    ClassLoaderService classLoaderService

    @Autowired
    TimeProvider timeProvider

    @Autowired
    InstanceLoader instanceLoader

    @Autowired
    MetaModelContextService metaModelContextService

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
        def sessionTimestamp = overiddenSessionTime != null ? overiddenSessionTime : 1

        if (expectedFileName == 'model_multiSourceExampleModel_to_class_ForTestMappingMultiSourceDto') {
            def metamodelContext = metaModelContextService.metaModelContext
            metamodelContext.mapperMetaModels.setMapperModelWithName("fromLocalDateToStringMapper", MapperMetaModel.builder()
                .mapperName("fromLocalDateToStringMapper")
                .mapperInstance(new LocalDateToStringMapperDummyGenerated())
                .build())
        }

        if (expectedFileName == "mapping_metamodel_with_enums_to_metamodel_with_enums") {
            def classMetaModels = metaModelContextService.metaModelContext.classMetaModels
            classMetaModels.put(1L, SOME_ENUM2_METAMODEL)
            classMetaModels.put(2L, SOME_ENUM1_METAMODEL)
            classMetaModels.put(3L, SOME_ENUM3_METAMODEL)
        }

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(newMapperGenerateConfiguration, sessionTimestamp)
        def result = mapperGenerator.generateMapperCode(mapperCodeMetadata)
        def compiledCodeMetadata = codeCompiler.compileCodeAndReturnMetaData(mapperCodeMetadata.getMapperClassName(), GENERATED_MAPPER_PACKAGE,
            result, sessionTimestamp)
        classLoaderService.createNewClassLoader(sessionTimestamp.toString())
        Class<?> mapperClass = classLoaderService.loadClass(compiledCodeMetadata.getFullClassName(), sessionTimestamp.toString())
        GeneratedMapper generatedMapper = instanceLoader.createInstanceOrGetBean(mapperClass)
        GenericMapperArgument finalGenericMapperArgument = genericMapperArgument.toBuilder()
            .sourceMetaModel(sourceMetaModel)
            .targetMetaModel(targetMetaModel)
            .build()

        then:
        saveMapperCodeToFile(result, sourceMetaModel, targetMetaModel, sessionTimestamp)
        makeLineEndingAsUnix(result) == makeLineEndingAsUnix(TemplateAsText.fromClassPath("expectedCode/" + expectedFileName).currentTemplateText)
        generatedMapper.mainMap(finalGenericMapperArgument.toBuilder().sourceObject(null).build()) == null
        def mappedObject = generatedMapper.mainMap(finalGenericMapperArgument)

        expectedResultObject == mappedObject

        where:
        sourceMetaModel                        | targetMetaModel                               | mapperGenerateConfiguration                   |
            expectedFileName                                                      | genericMapperArgument                                      |
            expectedResultObject                                                                                                                        |
            overiddenSessionTime

        modelFromClass(Long)                   | modelFromClass(Long)                          | EMPTY_CONFIG                                  |
            "simple_Long_to_Long"                                                 | emptyGenericMapperArgument(1L)                             | 1L     | null

        modelFromClass(Long)                   | modelFromClass(String)                        | EMPTY_CONFIG                                  |
            "simple_Long_to_String"                                               | emptyGenericMapperArgument(1L)                             | "1"    | null

        modelFromClass(String)                 | modelFromClass(FromStringToObject)            | STRING_TO_FROM_STRING_TO_OBJECT_CONF          |
            "text_to_FromStringToObject"                                          | emptyGenericMapperArgument("text")                         |
            new FromStringToObject("text")                                                                                                              | null

        modelFromClass(FromStringToObject)     | modelFromClass(String)                        | FROM_STRING_TO_OBJECT_CONF_TO_STRING          |
            "FromStringToObject_to_text"                                          | emptyGenericMapperArgument(new FromStringToObject("text")) | "text" | null

        modelFromClass(SamplePersonDto)        | getPersonMetaModel()                          | EMPTY_CONFIG                                  |
            "class_SamplePersonDto_to_model_person"                               | emptyGenericMapperArgument(SAMPLE_PERSON_DTO_AS_CLASS)     |
            PERSON_META_MODEL_AS_METAMODEL                                                                                                              | null

        // mapping from map to Dto via builder
        getPersonMetaModel()                   | modelFromClass(SamplePersonDto)               | EMPTY_CONFIG                                  |
            "model_person_to_class_SamplePersonDto"                               | emptyGenericMapperArgument(PERSON_META_MODEL_AS_METAMODEL) |
            SAMPLE_PERSON_DTO_AS_CLASS                                                                                                                  | null

        // mapping from map to Dto via builder, should get fields only from SomeDtoWithBuilder,
        // not from upper class due to @Builder only on SomeDtoWithBuilder class
        getSomeDtoWithBuilderModel()           | modelFromClass(SomeDtoWithBuilder)            | EMPTY_CONFIG                                  |
            "model_someDtoWithBuilder_to_class_SomeDtoWithBuilder"                | emptyGenericMapperArgument(DTO_WITH_BUILDER_MDL)           |
            DTO_WITH_BUILDER_CLS                                                                                                                        | null

        // mapping from map to Dto via builder, should get fields from whole @SuperBuilder hierarchy
        getSomeDtoWithSuperBuilderModel()      | modelFromClass(SomeDtoWithSimpleSuperBuilder) | EMPTY_CONFIG                                  |
            "model_someDtoWithSuperBuilderModel_to_class_SomeDtoWithSuperBuilder" | emptyGenericMapperArgument(DTO_WITH_SUPER_BUILDER_MDL)     |
            DTO_WITH_SUPER_BUILDER_CLS                                                                                                                  | null

        // mapping from map to simple Dto via all args
        getSomeSimpleValueDtoModel()           | modelFromClass(SomeSimpleValueDto)            | EMPTY_CONFIG                                  |
            "model_SomeSimpleValueDtoModel_to_class_SomeSimpleValueDto"           | emptyGenericMapperArgument([
            someLong3  : 11L,
            someString3: "someString3Va"
        ])                                                                                                                                     |
            new SomeSimpleValueDto("someString3Va", 11L, null)                                                                                          | null

        // mapping from map to simple Dto via setters
        getSomeDtoWithSettersModel()           | modelFromClass(SomeDtoWithSetters)            | EMPTY_CONFIG                                  |
            "model_SomeDtoWithSettersModel_to_class_SomeDtoWithSetters"           | emptyGenericMapperArgument(SOME_DTO_WITH_SETTERS_MDL
        )                                                                                                                                      |
            SOME_DTO_WITH_SETTERS_CLS                                                                                                                   | null

        // mapping with usage of genericObjectsConversionService and conversionService
        getClassHasSamplePersonModel1()        | modelFromClass(ClassHasSamplePersonDto)       |
            HAS_SAMPLE_PERSON_MODEL_1_CONF                                                                                                     |
            "model_ClassHasSamplePersonModel_to_class_ClassHasSamplePersonDto"    | emptyGenericMapperArgument(HAS_SAMPLE_PERSON_MDL_1)        |
            HAS_SAMPLE_PERSON_CLS_1                                                                                                                     | null

        //  mapping from map to Dto with nested methods and should use method when inner conversion is from person2 to SamplePersonDto in few fields
        //  person2 (metamodel) samplePersonDto -> SamplePersonDto samplePersonDto
        //  person2 (metamodel) otherPersonDto -> SamplePersonDto otherPersonDto
        //  used ignoredFields
        //  override field by get properties and by spring bean
        getClassHasSamplePersonModel2()        | modelFromClass(ClassHasSamplePersonDto)       |
            withMapperConfigurations(ignoredFieldsSamplePersonDtoConfig())                                                                     |
            "model_ClassHasSamplePersonModel2_to_class_ClassHasSamplePersonDto"   | CLASS_HAS_SAMPLE_PERSON_MODEL_2_AS_MODEL                   |
            CLASS_HAS_SAMPLE_PERSON_DTO_AS_CLS                                                                                                          | null

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
        // otherMappingForDocHolder.document.id = @fromLocalDateToStringMapper(documentDataPart1.localDateTime66)

        multiSourceExampleModel()              | modelFromClass(ForTestMappingMultiSourceDto)  | withMapperConfigurations(multiSourceConfig()) |
            "model_multiSourceExampleModel_to_class_ForTestMappingMultiSourceDto" | MULTI_SOURCE_EXAMPLE_MODEL_AS_MDL                          |
            FOR_TEST_MAPPING_MULTI_SOURCE_DTO_AS_CLS                                                                                                    | null

        // usage of nested configured method for mappings
        SOME_PERSON_MODEL1                     | modelFromClass(SomePerson1)                   | MAPPING_PERSON_1_CONFIG                       |
            "mapping_person1_model_to_class"                                      | SOME_PERSON_MODEL1_AS_MDL                                  |
            SOME_PERSON_1_AS_CLS                                                                                                                        | null

        // usage of nested configured method for mappings (from class to model) and globalIgnoreMappingProblems during map to target
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | EMPTY_CONFIG.toBuilder()
            .globalIgnoreMappingProblems(true)
            .build()                                                                                                                           |
            "mapping_person1_class_to_model_globalIgnoreMappingProblems"          | emptyGenericMapperArgument(SOME_PERSON_1_AS_CLS)           |
            SOME_PERSON_MODEL1_AS_MDL1                                                                                                                  | 0

        // usage of nested configured method for mappings (from class to model)
        //  ignoreMappingProblem via mapper configuration
        //  ignoreMappingProblem via override property
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | SOME_PERSON_MODEL1_FEW_IGNORED                |
            "mapping_person1_class_to_model_fewIgnored"                           | emptyGenericMapperArgument(
            SOME_PERSON_1_AS_CLS

        )                                                                                                                                      |
            SOME_PERSON_MODEL1_AS_MDL1                                                                                                                  | 2

        // should generate method for map inner object instead of use provided nested mapper method.
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | SOME_PERSON1_FROM_CLS_MDL_CONF                |
            "generate_method_instead_use_provided"                                | emptyGenericMapperArgument((SOME_PERSON_1_AS_CLS)
        )                                                                                                                                      |
            SOME_PERSON_MODEL1_AS_MDL2                                                                                                                  | 3

        // mapping collections elements
        modelFromClass(MappingCollections)     | MAPPING_COLLECTIONS_MODEL                     | EMPTY_CONFIG                                  |
            "mappingCollection_from_model_to_class"                               | emptyGenericMapperArgument(MAPPING_COLLECTIONS_AS_CLASS)   |
            MAPPING_COLLECTIONS_AS_MODEL                                                                                                                | null

        // mapping collections elements in the opposite way
        MAPPING_COLLECTIONS_MODEL              | modelFromClass(MappingCollections)            | EMPTY_CONFIG                                  |
            "mappingCollection_from_class_to_model"                               | emptyGenericMapperArgument(MAPPING_COLLECTIONS_AS_MODEL)   |
            MAPPING_COLLECTIONS_AS_CLASS                                                                                                                | null

        // mapping collections from one element to list via override properties
        // mapping collections from one element to array via override properties
        // mapping collections from one element to set via override properties

        // mapping collections from one element, from other list, from other set, from next one element to set via override properties
        // mapping collections elements by provided mapping method for each element in config
        // mapping collections elements by provided mapping method for whole elements (with the same value) in config
        MAPPING_COLLECTIONS_MODEL              | modelFromClass(MappingCollections)            | CUSTOM_MAPPING_COLLECTION_MDL_CLS             |
            "custom_mappingCollection_from_model_to_class"                        | emptyGenericMapperArgument(MAPPING_COLLECTIONS_AS_MODEL)   |
            CUSTOM_MAPPING_COLLECTION_CLS                                                                                                               | 2

        // convert java class to some java class
        modelFromClass(SomeDocument1)          | modelFromClass(SomeDocument1Entity)           | SOME_CLS_TO_CLS_CONF                          |
            "convert_java_class_to_some_java_class"                               | SOME_DOC_1_CLS                                             |
            SOME_DOC_EN_1_CLS                                                                                                                           | null

        // mapping from enum to metamodel of enum (as return in main method)
        modelFromClass(SomeEnum1)              | SOME_ENUM1_METAMODEL                          | EMPTY_CONFIG                                  |
            "from_enum_to_metamodel_enum_as_return_main_method"                   | emptyGenericMapperArgument(SomeEnum1.VAL1)                 | "VAL1" | null

        // mapping from enum to enum (as return in main method) and ignore some source enum entries
        modelFromClass(SomeEnum1)              | modelFromClass(SomeEnum2)                     | withMapperConfigurations(MapperConfiguration.builder()
            .enumEntriesMapping(EnumEntriesMapping.builder()
                .ignoredSourceEnum(["OTH", "VAL3"])
                .build())
            .build())                                                                                                                          |
            "from_enum_to_enum_as_return_main_method"                             | emptyGenericMapperArgument(SomeEnum1.VAL2)                 |
            SomeEnum2.VAL2                                                                                                                              | null

        // mapping from metamodel of enum to enum (as return in main method)
        SOME_ENUM1_METAMODEL                   | modelFromClass(SomeEnum1)                     | EMPTY_CONFIG                                  |
            "from_metamodel_enum_to_enum_as_return_main_method"                   | emptyGenericMapperArgument("VAL2")                         |
            SomeEnum1.VAL2                                                                                                                              | null

        // mapping from enum to metamodel of enum + overridden enums
        // default enum mapping (throw exception expression) for metamodel generic enum target

        // mapping from enum to enum + overridden enums
        // default enum mapping (other enum entry) for real enum target

        // mapping from metamodel of enum to enum + overridden enums
        // default enum mapping (other enum entry) for metamodel generic enum target

        // mapping from metamodel of enum to metamodel of enum  + overridden enums
        // default enum mapping (throw exception expression) for real enum target

        // mapping from string to metamodel of enum
        // mapping from metamodel of enum to string
        // mapping from string to enum by native spring conversions
        // mapping from string to enum by crud wizard service conversion
        // mapping from string to enum meta model by crud wizard service conversion
        METAMODEL_WITH_ENUMS1                  | METAMODEL_WITH_ENUMS2                         | FEW_ENUM_MAPPINGS_CONF                        |
            "mapping_metamodel_with_enums_to_metamodel_with_enums"                | METAMODEL_WITH_ENUMS1_MDL                                  |
            METAMODEL_WITH_ENUMS2_MDL                                                                                                                   | null

        // resolving conflicts when exists the same inner method (but other name) for mapping enum
        METAMODEL_WITH_ENUMS3                  | METAMODEL_WITH_ENUMS4                         | ENUM_CONFLICTS_CONF                           |
            "resolve_enums_map_method_conflict"                                   | METAMODEL_WITH_ENUMS3_MDL                                  |
            METAMODEL_WITH_ENUMS4_MDL                                                                                                                   | null

        // test for resolve conflict when are two inner method for mapping list elements by override with name
        //  of mapping method with path like 'elements1.*'
        //  and second one for 'elements2.*'
        //  mapping like 'elements3.*.field1=null'
        OTHER_WITH_ELEMENTS_MODEL              | modelFromClass(OtherWithElements)             | MAPPING_COLLECTION_WITH_STAR_CONF             |
            "resolve_in_collection_mapping_conflict"                              | OTHER_WITH_ELEMENTS_MDL                                    |
            OTHER_WITH_ELEMENTS_CLS                                                                                                                     | null

        // test for resolve conflict when are two inner method for mapping list elements by override with name
        //  of mapping method with path like
        //  'elements1=someOneElement.eachMapBy(mapElements1)'
        //  'elements1=elements1.eachMapBy(mapElements1)'
        //  'elements1=someOneElement2.eachMapBy(mapElements1)'
        //  and second one for 'elements2.*'
        //  mapping like 'elements3.*.field1=null'
        OTHER_WITH_ELEMENTS_MODEL              | modelFromClass(OtherWithElements)             | EACH_MAP_BY_CONF                              |
            "resolve_in_collection_mapping_conflict2"                             | OTHER_WITH_ELEMENTS_MDL                                    |
            OTHER_WITH_ELEMENTS_CLS2                                                                                                                    | 2

        // mapping Set<elementModel> to List<CollectionElement> in main method
        LIST_OF_METAMODELS                     | SET_OF_CLASSES                                | EMPTY_CONFIG                                  |
            "main_method_returns_set_when_source_is_list"                         | LIST_OF_METAMODELS_SAMPLE                                  |
            LIST_OF_ELEMENTS_CLS                                                                                                                        | null

        // mapping of List<List<elementModel> to List<Set<CollectionElement>> in main method
        LIST_LIST_MODEL                        | LIST_SET_ELEMENT                              | EMPTY_CONFIG                                  |
            "main_method_returns_set_of_lists_when_source_is_list_of_list"        | LIST_LIST_MODEL_SAMPLE                                     |
            LIST_SET_ELEMENT_SAMPLE                                                                                                                     | null

        // mapping of List<List<CollectionElement>> to Set<NestedCollectionElement[]> inside of other object
        modelFromClass(ListAsListGenericType1) | modelFromClass(ListAsListGenericType2)        | EMPTY_CONFIG                                  |
            "mapping_collections_as_generic_type_in_object"                       | LIST_AS_LIST_GENERIC_TYPE_1_CLS                            |
            LIST_AS_LIST_GENERIC_TYPE_2_CLS                                                                                                             | null
    }

    def "not found mapping way for object fields via main method chain"() {
        given:
        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL)
            .targetMetaModel(modelFromClass(ObjectForNotFondMappings))
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        mapperGenerator.generateMapperCode(mapperCodeMetadata)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 4
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "name", ObjectForNotFondMappings.NestedNotFound.canonicalName, "someObject.name", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "someLong", ObjectForNotFondMappings.canonicalName, "someLong", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "uuid", ObjectForNotFondMappings.InsideCollectionElement.canonicalName, "list.*.uuid", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "uuid", ObjectForNotFondMappings.InsideCollectionElement.canonicalName, "listOfList.*.*.uuid", "")
            .translateMessage()
    }

    def "not found mapping way for object fields via other method"() {
        given:
        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL)
            .targetMetaModel(modelFromClass(ObjectForNotFondMappings))
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        mapperGenerateConf.addSubMapperConfiguration("mapInsideCollectionElement",
            MapperConfiguration.builder()
                .name("mapInsideCollectionElement")
                .sourceMetaModel(INSIDE_COLLECTION_ELEMENT)
                .targetMetaModel(modelFromClass(ObjectForNotFondMappings.InsideCollectionElement))
                .build())

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        mapperGenerator.generateMapperCode(mapperCodeMetadata)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 3
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "name", ObjectForNotFondMappings.NestedNotFound.canonicalName, "someObject.name", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "someLong", ObjectForNotFondMappings.canonicalName, "someLong", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "uuid", ObjectForNotFondMappings.InsideCollectionElement.canonicalName, "uuid",
            occurredInNotGeneratedMethod("mapInsideCollectionElement"))
            .translateMessage()
    }

    def "for not found mapping way for enum mappings"() {
        given:
        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(MODEL_WITH_ENUM)
            .targetMetaModel(modelFromClass(ObjectWithEnum))
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        mapperGenerator.generateMapperCode(mapperCodeMetadata)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 2
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.cannot.map.enum.value",
            "UNKNOWN", "exampleEnum", ExampleEnum2.canonicalName, "otherObjectWithEnum.otherEnum")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.cannot.map.enum.value",
            "UNKNOWN", "exampleEnum", ExampleEnum2.canonicalName, "someEnum")
            .translateMessage()
    }

    def "not found mapping for enum in not generated method"() {
        given:
        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(MODEL_WITH_ENUM)
            .targetMetaModel(modelFromClass(ObjectWithEnum))
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        mapperGenerateConf.addSubMapperConfiguration("mapEnum",
            MapperConfiguration.builder()
                .name("mapEnum")
                .sourceMetaModel(EXAMPLE_ENUM_MODEL)
                .targetMetaModel(modelFromClass(ExampleEnum2))
                .build())

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        mapperGenerator.generateMapperCode(mapperCodeMetadata)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 1
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.cannot.map.enum.value",
            "UNKNOWN", "exampleEnum", ExampleEnum2.canonicalName, "",
            occurredInNotGeneratedMethod("mapEnum"))
            .translateMessage()
    }

    def "found to many mappers methods for enum field"() {
        given:
        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(modelFromClass(ObjectWithEnum.OtherObjectWithEnum))
            .targetMetaModel(OTHER_OBJECT_WITH_ENUM_MODEL)
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        mapperGenerateConf.addSubMapperConfiguration("mapEnum1",
            MapperConfiguration.builder()
                .name("mapEnum1")
                .sourceMetaModel(modelFromClass(ExampleEnum2))
                .targetMetaModel(EXAMPLE_ENUM_MODEL)
                .build())

        mapperGenerateConf.addSubMapperConfiguration("mapEnum2",
            MapperConfiguration.builder()
                .name("mapEnum2")
                .sourceMetaModel(modelFromClass(ExampleEnum2))
                .targetMetaModel(EXAMPLE_ENUM_MODEL)
                .build())

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        mapperGenerator.generateMapperCode(mapperCodeMetadata)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 1
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "otherEnum", OTHER_OBJECT_WITH_ENUM_MODEL.name, "otherEnum",
            errorReason(createMessagePlaceholder("mapper.found.to.many.methods", "mapEnum2, mapEnum1")))
            .translateMessage()
    }

    def "found to many mappers methods for object field, collection element"() {
        given:
        def sourceCollectionElement = ClassMetaModel.builder()
            .name("sourceCollectionElement")
            .fields([
                createValidFieldMetaModel("someField", Long),
            ])
            .build()

        def sourceNestedObject = ClassMetaModel.builder()
            .name("sourceNestedObject")
            .fields([
                createValidFieldMetaModel("someField1", Long),
                createValidFieldMetaModel("list", createClassModelWithGenerics(List, sourceCollectionElement)),
                createValidFieldMetaModel("someField2", Long),
            ])
            .build()

        def sourceModel = ClassMetaModel.builder()
            .name("sourceModel")
            .fields([
                createValidFieldMetaModel("someObject", sourceNestedObject),
                createValidFieldMetaModel("someString", String),
            ])
            .build()

        def targetCollectionElement = ClassMetaModel.builder()
            .name("targetCollectionElement")
            .fields([
                createValidFieldMetaModel("someField", Long),
            ])
            .build()

        def targetNestedObject = ClassMetaModel.builder()
            .name("targetNestedObject")
            .fields([
                createValidFieldMetaModel("someField1", String),
                createValidFieldMetaModel("someField2", Long),
                createValidFieldMetaModel("list", createClassModelWithGenerics(List, targetCollectionElement)),
            ])
            .build()

        def targetModel = ClassMetaModel.builder()
            .name("targetModel")
            .fields([
                createValidFieldMetaModel("someObject", targetNestedObject),
                createValidFieldMetaModel("someString", String)
            ])
            .build()

        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(sourceModel)
            .targetMetaModel(targetModel)
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        mapperGenerateConf.addSubMapperConfiguration("mapNestedObject1",
            MapperConfiguration.builder()
                .name("mapNestedObject1")
                .sourceMetaModel(sourceNestedObject)
                .targetMetaModel(targetNestedObject)
                .build())

        mapperGenerateConf.addSubMapperConfiguration("mapNestedObject2",
            MapperConfiguration.builder()
                .name("mapNestedObject2")
                .sourceMetaModel(sourceNestedObject)
                .targetMetaModel(targetNestedObject)
                .build())

        mapperGenerateConf.addSubMapperConfiguration("mapCollectionElement1",
            MapperConfiguration.builder()
                .name("mapCollectionElement1")
                .sourceMetaModel(sourceCollectionElement)
                .targetMetaModel(targetCollectionElement)
                .build())

        mapperGenerateConf.addSubMapperConfiguration("mapCollectionElement2",
            MapperConfiguration.builder()
                .name("mapCollectionElement2")
                .sourceMetaModel(sourceCollectionElement)
                .targetMetaModel(targetCollectionElement)
                .build())

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperCodeMetadata),
            sourceModel, targetModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 3
        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "*", "targetCollectionElement", "list.*",
            occurredInNotGeneratedMethod("mapNestedObject1") +
                errorReason(createMessagePlaceholder("mapper.found.to.many.methods",
                    "mapCollectionElement2, mapCollectionElement1")))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "*", "targetCollectionElement", "list.*",
            occurredInNotGeneratedMethod("mapNestedObject2") +
                errorReason(createMessagePlaceholder("mapper.found.to.many.methods",
                    "mapCollectionElement2, mapCollectionElement1")))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "someObject", "targetModel", "someObject",
            errorReason(createMessagePlaceholder("mapper.found.to.many.methods",
                "mapNestedObject1, mapNestedObject2")))
            .translateMessage()
    }

    def "find to many mapping way for simple field via direct override and via parent override few sources and problem with find conversion way"() {
        given:
        def sourceNestedClassMetaModel1 = ClassMetaModel.builder()
            .name("sourceNestedClassMetaModel1")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
            ])
            .build()

        def sourceNestedClassMetaModel2 = ClassMetaModel.builder()
            .name("sourceNestedClassMetaMode2")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
                createValidFieldMetaModel("someLong", Long),
            ])
            .build()

        def sourceClassMetaModel = ClassMetaModel.builder()
            .name("sourceClassMetaModel")
            .fields([
                createValidFieldMetaModel("someText", ClassMetaModel.builder()
                    .name("otherModel")
                    .fields([
                        createValidFieldMetaModel("someText", String),
                        createValidFieldMetaModel("cratedInDate", LocalDate),
                    ])
                    .build()),
                createValidFieldMetaModel("toManyDirectly", Long),
                createValidFieldMetaModel("nestedObject1", sourceNestedClassMetaModel1),
                createValidFieldMetaModel("nestedObject2", sourceNestedClassMetaModel2),
            ])
            .build()

        def targetNestedClassMetaModel = ClassMetaModel.builder()
            .name("targetNestedClassMetaModel")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
                createValidFieldMetaModel("someLong", Long),
            ])
            .build()

        def targetClassMetaModel = ClassMetaModel.builder()
            .name("targetClassMetaModel")
            .fields([
                createValidFieldMetaModel("toManyDirectly", Long),
                createValidFieldMetaModel("nestedObject", targetNestedClassMetaModel),
                createValidFieldMetaModel("someText", String),
            ])
            .build()

        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(sourceClassMetaModel)
            .targetMetaModel(targetClassMetaModel)
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([

                    // via concrete assign to this field by two sources
                    toManyDirectly: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(sourceClassMetaModel, "toManyDirectly"),
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject2")
                                .createExpressionWithNextField("someLong", READ_FIELD_RESOLVER_CONFIG)
                        ])
                        .build(),

                    //  via assign to object which contains simple field,
                    //  but for this assigned object given was two sources
                    //  (every of that source have that the same name of simple field)
                    nestedObject  : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject1"),
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject2")
                        ])
                        .build()
                ])
                .build())
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperCodeMetadata),
            sourceClassMetaModel, targetClassMetaModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 3

        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "otherModel", "java.lang.String", "someText", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "toManyDirectly", "targetClassMetaModel", "toManyDirectly",
            errorReason("{mapper.found.to.many.mappings.for.simple.type}"))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "toManyViaParent", "targetNestedClassMetaModel", "nestedObject.toManyViaParent",
            errorReason("{mapper.found.to.many.mappings.for.simple.type}"))
            .translateMessage()
    }

    def "marked few ignoreMappingProblem during map to target, in some classes, but finally will return exception"() {
        given:
        def nextSourceObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Long),
            ])
            .build()

        def sourceNestedClassMetaModel1 = ClassMetaModel.builder()
            .name("sourceNestedClassMetaModel1")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
            ])
            .build()

        def sourceNestedClassMetaModel2 = ClassMetaModel.builder()
            .name("sourceNestedClassMetaMode2")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
                createValidFieldMetaModel("someLong", Long),
            ])
            .build()

        def sourceClassMetaModel = ClassMetaModel.builder()
            .name("sourceClassMetaModel")
            .fields([
                createValidFieldMetaModel("someText", ClassMetaModel.builder()
                    .name("otherModel")
                    .fields([
                        createValidFieldMetaModel("someText", String),
                        createValidFieldMetaModel("cratedInDate", LocalDate),
                    ])
                    .build()),
                createValidFieldMetaModel("toManyDirectly", Long),
                createValidFieldMetaModel("nestedObject1", sourceNestedClassMetaModel1),
                createValidFieldMetaModel("nestedObject2", sourceNestedClassMetaModel2),
                createValidFieldMetaModel("nextObjectField", nextSourceObject),
            ])
            .build()

        def targetNestedClassMetaModel = ClassMetaModel.builder()
            .name("targetNestedClassMetaModel")
            .fields([
                createValidFieldMetaModel("toManyViaParent", Long),
                createValidFieldMetaModel("someLong", Long),
            ])
            .build()

        def nextTargetObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Long),
                createValidFieldMetaModel("secondField", Long),
                createValidFieldMetaModel("thirdField", String),
            ])
            .build()

        def targetClassMetaModel = ClassMetaModel.builder()
            .name("targetClassMetaModel")
            .fields([
                createValidFieldMetaModel("toManyDirectly", Long),
                createValidFieldMetaModel("nestedObject", targetNestedClassMetaModel),
                createValidFieldMetaModel("nextObjectField", nextTargetObject),
                createValidFieldMetaModel("someText", String),
            ])
            .build()

        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(sourceClassMetaModel)
            .targetMetaModel(targetClassMetaModel)
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([

                    // via concrete assign to this field by two sources
                    toManyDirectly: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(sourceClassMetaModel, "toManyDirectly"),
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject2")
                                .createExpressionWithNextField("someLong", READ_FIELD_RESOLVER_CONFIG)
                        ])
                        .build(),

                    //  via assign to object which contains simple field,
                    //  but for this assigned object given was two sources
                    //  (every of that source have that the same name of simple field)
                    nestedObject  : PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject1"),
                            createFieldsChainExpression(sourceClassMetaModel, "nestedObject2")
                        ])
                        .build(),
                    someText      : PropertiesOverriddenMapping.builder()
                        .ignoreMappingProblem(true)
                        .build()
                ])
                .build())
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .build()

        def otherMethod = MapperConfiguration.builder()
            .name("mapNextObjectField")
            .sourceMetaModel(nextSourceObject)
            .targetMetaModel(nextTargetObject)
            .ignoreMappingProblems(true)
            .build()

        mapperGenerateConf.addSubMapperConfiguration(otherMethod.getName(), otherMethod)

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperCodeMetadata),
            sourceClassMetaModel, targetClassMetaModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 2

        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "toManyDirectly", "targetClassMetaModel", "toManyDirectly",
            errorReason("{mapper.found.to.many.mappings.for.simple.type}"))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.not.found.assign.strategy",
            "toManyViaParent", "targetNestedClassMetaModel", "nestedObject.toManyViaParent",
            errorReason("{mapper.found.to.many.mappings.for.simple.type}"))
            .translateMessage()
    }

    def "not use auto conversion of types when disabled globally"() {
        given:
        def nextSourceObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Long),
                createValidFieldMetaModel("field2", String),
                createValidFieldMetaModel("thirdField", String),
            ])
            .build()

        def sourceClassMetaModel = ClassMetaModel.builder()
            .name("sourceClassMetaModel")
            .fields([
                createValidFieldMetaModel("someId", Long),
                createValidFieldMetaModel("nextObjectField", nextSourceObject),
            ])
            .build()

        def nextTargetObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Double),
                createValidFieldMetaModel("secondField", Long),
                createValidFieldMetaModel("thirdField", String),
            ])
            .build()

        def targetClassMetaModel = ClassMetaModel.builder()
            .name("targetClassMetaModel")
            .fields([
                createValidFieldMetaModel("nextObjectField", nextTargetObject),
                createValidFieldMetaModel("someId", String),
            ])
            .build()

        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(sourceClassMetaModel)
            .targetMetaModel(targetClassMetaModel)
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .globalEnableAutoMapping(false)
            .build()

        def otherMethod = MapperConfiguration.builder()
            .name("mapNextObjectField")
            .sourceMetaModel(nextSourceObject)
            .targetMetaModel(nextTargetObject)
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName(
                    secondField: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([createFieldsChainExpression(nextSourceObject, "field2")])
                        .build()
                )
                .build())
            .build()

        mapperGenerateConf.addSubMapperConfiguration(otherMethod.getName(), otherMethod)

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperCodeMetadata),
            sourceClassMetaModel, targetClassMetaModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 3

        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "java.lang.Long", "java.lang.Double", "firstField",
            occurredInNotGeneratedMethod("mapNextObjectField"))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "java.lang.Long", "java.lang.String", "someId", "")
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "java.lang.String", "java.lang.Long", "secondField",
            occurredInNotGeneratedMethod("mapNextObjectField"))
            .translateMessage()
    }

    def "not use auto conversion of types when disabled in other mapper method"() {
        given:
        def nextSourceObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Long),
                createValidFieldMetaModel("field2", String),
                createValidFieldMetaModel("thirdField", String),
            ])
            .build()

        def sourceClassMetaModel = ClassMetaModel.builder()
            .name("sourceClassMetaModel")
            .fields([
                createValidFieldMetaModel("someId", Long),
                createValidFieldMetaModel("nextObjectField", nextSourceObject),
            ])
            .build()

        def nextTargetObject = ClassMetaModel.builder()
            .name("nextSourceObject")
            .fields([
                createValidFieldMetaModel("firstField", Double),
                createValidFieldMetaModel("secondField", Long),
                createValidFieldMetaModel("thirdField", String),
            ])
            .build()

        def targetClassMetaModel = ClassMetaModel.builder()
            .name("targetClassMetaModel")
            .fields([
                createValidFieldMetaModel("nextObjectField", nextTargetObject),
                createValidFieldMetaModel("someId", String),
            ])
            .build()

        def mainMethodConfig = MapperConfiguration.builder()
            .sourceMetaModel(sourceClassMetaModel)
            .targetMetaModel(targetClassMetaModel)
            .build()

        def mapperGenerateConf = MapperGenerateConfiguration.builder()
            .rootConfiguration(mainMethodConfig)
            .globalEnableAutoMapping(true)
            .build()

        def otherMethod = MapperConfiguration.builder()
            .name("mapNextObjectField")
            .sourceMetaModel(nextSourceObject)
            .targetMetaModel(nextTargetObject)
            .enableAutoMapping(false)
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName(
                    secondField: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([createFieldsChainExpression(nextSourceObject, "field2")])
                        .build()
                )
                .build())
            .build()

        mapperGenerateConf.addSubMapperConfiguration(otherMethod.getName(), otherMethod)

        when:
        def mapperCodeMetadata = mapperGenerator.generateMapperCodeMetadata(mapperGenerateConf, 1)
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperCodeMetadata),
            sourceClassMetaModel, targetClassMetaModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 2

        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "java.lang.Long", "java.lang.Double", "firstField",
            occurredInNotGeneratedMethod("mapNextObjectField"))
            .translateMessage()

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "java.lang.String", "java.lang.Long", "secondField",
            occurredInNotGeneratedMethod("mapNextObjectField"))
            .translateMessage()
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
                    .build()),
                createValidFieldMetaModel("testCurrentNodeObjectWrapper", ClassMetaModel.builder()
                    .name("testCurrentNodeObjectWrapperModel")
                    .fields([
                        createValidFieldMetaModel("testCurrentNodeObject", ForTestMappingMultiSourceDto.TestCurrentNodeObjectInModel),
                        createValidFieldMetaModel("simpleCurrentNodeTest", String)
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
                .valueMappingStrategy([
                    new RawJavaCodeAssignExpression(multiSourceExampleModel, "sourceObject"),
                    createFieldsChainExpression(multiSourceExampleModel, "testCurrentNodeObjectWrapper")
                ])
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

    private static List<String> getSortedMessages(MapperGenerationException mapperGenerationException) {
        mapperGenerationException.messagePlaceholders
            .collect {
                it.translateMessage()
            }
            .sort()
    }

    private static Iterator<String> getSortedMessagesIterator(MapperGenerationException mapperGenerationException) {
        getSortedMessages(mapperGenerationException).iterator()
    }

    private static String errorReason(Object reasonArgument) {
        " " + createMessagePlaceholder("mapper.mapping.problem.reason", reasonArgument)
    }

    private static void saveMapperCodeToFile(String code, ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel, Long sessionTimestamp = 0) {
        def folderPath = "target/generated-test-sources/mappers/pl/jalokim/crudwizard/generated/mapper"
        FileUtils.createDirectories(folderPath)
        def mapperClassName = String.format("%sTo%sMapper%s",
            getClassModelInfoForGeneratedCode(sourceMetaModel),
            getClassModelInfoForGeneratedCode(targetMetaModel),
            sessionTimestamp
        )
        FileUtils.writeToFile(String.format("%s/%s.java", folderPath, mapperClassName), code)
    }
}
