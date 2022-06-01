package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassModelWithGenerics
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidEnumMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.makeLineEndingAsUnix
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.AssignExpressionAsTextResolver.occurredInNotGeneratedMethod
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression.createRawJavaCodeExpression

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.ClassHasSamplePersonDto
import pl.jalokim.crudwizard.core.sample.ForTestMappingMultiSourceDto
import pl.jalokim.crudwizard.core.sample.FromStringToObject
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSimpleSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElementOther
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ExampleEnum2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.MappingCollections
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ObjectForNotFondMappings
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ObjectWithEnum
import pl.jalokim.crudwizard.genericapp.mapper.conversion.OtherWithElements
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeContact1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1Entity
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum3
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomePerson1
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.EnumEntriesMapping
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.EachElementMapByMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.validation.MapperGenerationException
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
        def result = mapperGenerator.generateMapperCode(newMapperGenerateConfiguration)

        then:
        saveMapperCodeToFile(result, sourceMetaModel, targetMetaModel)
        makeLineEndingAsUnix(result) == makeLineEndingAsUnix(TemplateAsText.fromClassPath("expectedCode/" + expectedFileName).currentTemplateText)

        where:
        sourceMetaModel                        | targetMetaModel                               | mapperGenerateConfiguration                   |
            expectedFileName

        modelFromClass(Long)                   | modelFromClass(Long)                          | EMPTY_CONFIG                                  |
            "simple_Long_to_Long"

        modelFromClass(Long)                   | modelFromClass(String)                        | EMPTY_CONFIG                                  |
            "simple_Long_to_String"

        modelFromClass(String)                 | modelFromClass(FromStringToObject)            | withMapperConfigurations(
            MapperConfiguration.builder()
                .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([
                        new RawJavaCodeAssignExpression(modelFromClass(FromStringToObject),
                            "new " + FromStringToObject.canonicalName + "((String) sourceObject)")
                    ])
                    .build())
                .build())                                                                                                                      |
            "text_to_FromStringToObject"

        modelFromClass(FromStringToObject)     | modelFromClass(String)                        | withMapperConfigurations(
            MapperConfiguration.builder()
                .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([
                        createFieldsChainExpression(modelFromClass(FromStringToObject), "sourceText")
                    ])
                    .build())
                .build())                                                                                                                      |
            "FromStringToObject_to_text"

        modelFromClass(SamplePersonDto)        | getPersonMetaModel()                          | EMPTY_CONFIG                                  |
            "class_SamplePersonDto_to_model_person"

        // mapping from map to Dto via builder
        getPersonMetaModel()                   | modelFromClass(SamplePersonDto)               | EMPTY_CONFIG                                  |
            "model_person_to_class_SamplePersonDto"

        // mapping from map to Dto via builder, should get fields only from SomeDtoWithBuilder,
        // not from upper class due to @Builder only on SomeDtoWithBuilder classs
        getSomeDtoWithBuilderModel()           | modelFromClass(SomeDtoWithBuilder)            | EMPTY_CONFIG                                  |
            "model_someDtoWithBuilder_to_class_SomeDtoWithBuilder"

        // mapping from map to Dto via builder, should get fields from whole @SuperBuilder hierarchy
        getSomeDtoWithSuperBuilderModel()      | modelFromClass(SomeDtoWithSimpleSuperBuilder) | EMPTY_CONFIG                                  |
            "model_someDtoWithSuperBuilderModel_to_class_SomeDtoWithSuperBuilder"

        // mapping from map to simple Dto via all args
        getSomeSimpleValueDtoModel()           | modelFromClass(SomeSimpleValueDto)            | EMPTY_CONFIG                                  |
            "model_SomeSimpleValueDtoModel_to_class_SomeSimpleValueDto"

        // mapping from map to simple Dto via setters
        getSomeDtoWithSettersModel()           | modelFromClass(SomeDtoWithSetters)            | EMPTY_CONFIG                                  |
            "model_SomeDtoWithSettersModel_to_class_SomeDtoWithSetters"

        // mapping with usage of genericObjectsConversionService and conversionService
        getClassHasSamplePersonModel1()        | modelFromClass(ClassHasSamplePersonDto)       |
            withMapperConfigurations(MapperConfiguration.builder()
                .propertyOverriddenMapping(PropertiesOverriddenMapping.builder().ignoredFields(["someObjectWithFewObjects"]).build())
                .build()
            )                                                                                                                                  |
            "model_ClassHasSamplePersonModel_to_class_ClassHasSamplePersonDto"

        //  mapping from map to Dto with nested methods and should use method when inner conversion is from person2 to SamplePersonDto in few fields
        //  person2 (metamodel) samplePersonDto -> SamplePersonDto samplePersonDto
        //  person2 (metamodel) otherPersonDto -> SamplePersonDto otherPersonDto
        //  used ignoredFields
        //  override field by get properties and by spring bean
        getClassHasSamplePersonModel2()        | modelFromClass(ClassHasSamplePersonDto)       |
            withMapperConfigurations(ignoredFieldsSamplePersonDtoConfig())                                                                     |
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

        multiSourceExampleModel()              | modelFromClass(ForTestMappingMultiSourceDto)  | withMapperConfigurations(multiSourceConfig()) |
            "model_multiSourceExampleModel_to_class_ForTestMappingMultiSourceDto"

        // usage of nested configured method for mappings
        SOME_PERSON_MODEL1                     | modelFromClass(SomePerson1)                   | MAPPING_PERSON_1_CONFIG                       |
            "mapping_person1_model_to_class"

        // usage of nested configured method for mappings (from class to model) and globalIgnoreMappingProblems during map to target
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | EMPTY_CONFIG.toBuilder()
            .globalIgnoreMappingProblems(true)
            .build()                                                                                                                           |
            "mapping_person1_class_to_model_globalIgnoreMappingProblems"

        // usage of nested configured method for mappings (from class to model)
        //  ignoreMappingProblem via mapper configuration
        //  ignoreMappingProblem via override property
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | SOME_PERSON_MODEL1_FEW_IGNORED                |
            "mapping_person1_class_to_model_fewIgnored"

        // should generate method for map inner object instead of use provided nested mapper method.
        modelFromClass(SomePerson1)            | SOME_PERSON_MODEL1                            | withMapperConfigurations(
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
        )                                                                                                                                      |
            "generate_method_instead_use_provided"

        // mapping collections elements
        modelFromClass(MappingCollections)     | MAPPING_COLLECTIONS_MODEL                     | EMPTY_CONFIG                                  |
            "mappingCollection_from_model_to_class"

        // mapping collections elements in the opposite way
        MAPPING_COLLECTIONS_MODEL              | modelFromClass(MappingCollections)            | EMPTY_CONFIG                                  |
            "mappingCollection_from_class_to_model"

        // mapping collections from one element to list via override properties
        // mapping collections from one element to array via override properties
        // mapping collections from one element to set via override properties

        // mapping collections from one element, from other list, from other set, from next one element to set via override properties
        // mapping collections elements by provided mapping method for each element in config
        // mapping collections elements by provided mapping method for whole elements (with the same value) in config
        MAPPING_COLLECTIONS_MODEL              | modelFromClass(MappingCollections)            | withMapperConfigurations(MapperConfiguration.builder()
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
        )                                                                                                                                      |
            "custom_mappingCollection_from_model_to_class"

        // convert java class to some java class
        modelFromClass(SomeDocument1)          | modelFromClass(SomeDocument1Entity)           | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    someLocalDate: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([createFieldsChainExpression(modelFromClass(SomeDocument1), "validToDate")])
                        .build()
                ])
                .build())
            .build())                                                                                                                          |
            "convert_java_class_to_some_java_class"

        // mapping from enum to metamodel of enum (as return in main method)
        modelFromClass(SomeEnum1)              | SOME_ENUM1_METAMODEL                          | EMPTY_CONFIG                                  |
            "from_enum_to_metamodel_enum_as_return_main_method"

        // mapping from enum to enum (as return in main method) and ignore some source enum entries
        modelFromClass(SomeEnum1)              | modelFromClass(SomeEnum2)                     | withMapperConfigurations(MapperConfiguration.builder()
            .enumEntriesMapping(EnumEntriesMapping.builder()
                .ignoredSourceEnum(["OTH", "VAL3"])
                .build())
            .build())                                                                                                                          |
            "from_enum_to_enum_as_return_main_method"

        // mapping from metamodel of enum to enum (as return in main method)
        SOME_ENUM1_METAMODEL                   | modelFromClass(SomeEnum1)                     | EMPTY_CONFIG                                  |
            "from_metamodel_enum_to_enum_as_return_main_method"

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
        METAMODEL_WITH_ENUMS1                  | METAMODEL_WITH_ENUMS2                         | withMapperConfigurations(
            MapperConfiguration.builder().build(),

            MapperConfiguration.builder()
                .name("mapFromEnumToEnumMetaModel")
                .sourceMetaModel(modelFromClass(SomeEnum1))
                .targetMetaModel(SOME_ENUM1_METAMODEL)
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "UNKNOWN": "VAL1",
                    ])
                    .whenNotMappedEnum("throw new IllegalArgumentException(\"cannot map enum with value: \" + sourceObject)")
                    .build())
                .build(),

            MapperConfiguration.builder()
                .name("mapFromEnumToEnum")
                .sourceMetaModel(modelFromClass(SomeEnum1))
                .targetMetaModel(modelFromClass(SomeEnum2))
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "VAL3": "OTH2",
                        "OTH" : "OTH1",
                    ])
                    .whenNotMappedEnum("UNKNOWN")
                    .build())
                .build(),

            MapperConfiguration.builder()
                .name("mapFromEnumMetaModelToEnum")
                .sourceMetaModel(SOME_ENUM2_METAMODEL)
                .targetMetaModel(modelFromClass(SomeEnum2))
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "VAL2": "OTH2",
                    ])
                    .ignoredSourceEnum(["OTH1", "UNKNOWN"])
                    .whenNotMappedEnum("UNKNOWN")
                    .build())
                .build(),

            MapperConfiguration.builder()
                .name("mapFromEnumMetaModelToEnumMetaModel")
                .sourceMetaModel(SOME_ENUM1_METAMODEL)
                .targetMetaModel(SOME_ENUM2_METAMODEL)
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "OTH": "OTH1",
                    ])
                    .ignoredSourceEnum(["VAL3"])
                    .whenNotMappedEnum("throw new IllegalArgumentException(sourceObject)")
                    .build())
                .build()
        )                                                                                                                                      |
            "mapping_metamodel_with_enums_to_metamodel_with_enums"

        // resolving conflicts when exists the same inner method (but other name) for mapping enum
        METAMODEL_WITH_ENUMS3                  | METAMODEL_WITH_ENUMS4                         | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    spec1enum: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new MethodInCurrentClassAssignExpression("spec1enumMap",
                                [createFieldsChainExpression(METAMODEL_WITH_ENUMS3, "spec1enum")],
                                modelFromClass(SomeEnum3))])
                        .build(),
                    spec2enum: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new MethodInCurrentClassAssignExpression("spec2enumMap",
                                [createFieldsChainExpression(METAMODEL_WITH_ENUMS3, "spec2enum")],
                                modelFromClass(SomeEnum3))])
                        .build(),
                ])
                .build())
            .build(),
            MapperConfiguration.builder()
                .name("spec1enumMap")
                .sourceMetaModel(SOME_ENUM1_METAMODEL)
                .targetMetaModel(modelFromClass(SomeEnum3))
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "OTH": "SPEC1",
                    ])
                    .ignoredSourceEnum(["UNKNOWN"])
                    .build())
                .build(),
            MapperConfiguration.builder()
                .name("spec2enumMap")
                .sourceMetaModel(SOME_ENUM1_METAMODEL)
                .targetMetaModel(modelFromClass(SomeEnum3))
                .enumEntriesMapping(EnumEntriesMapping.builder()
                    .targetEnumBySourceEnum([
                        "OTH": "SPEC2",
                    ])
                    .ignoredSourceEnum(["UNKNOWN"])
                    .build())
                .build(),
        )                                                                                                                                      |
            "resolve_enums_map_method_conflict"

        // test for resolve conflict when are two inner method for mapping list elements by override with name
        //  of mapping method with path like 'elements1.*'
        //  and second one for 'elements2.*'
        //  mapping like 'elements3.*.field1=null'
        OTHER_WITH_ELEMENTS_MODEL              | modelFromClass(OtherWithElements)             | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    elements1: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "someOneElement"),
                            createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "elements1"),
                            new MethodInCurrentClassAssignExpression("mapFromOtherElement",
                                [createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "someOneElement2")],
                                modelFromClass(CollectionElement))
                        ])
                        .mappingsByPropertyName([
                            '*': PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    new MethodInCurrentClassAssignExpression("mapElements1",
                                        List.of(new RawJavaCodeAssignExpression(COLLECTION_ELEMENT_MODEL, "sourceObject")),
                                        modelFromClass(CollectionElement))
                                ])
                                .build()
                        ])
                        .build(),
                    elements2: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            '*': PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    new MethodInCurrentClassAssignExpression("mapElements2",
                                        List.of(new RawJavaCodeAssignExpression(COLLECTION_ELEMENT_MODEL, "sourceObject")),
                                        modelFromClass(CollectionElement))
                                ])
                                .build()
                        ])
                        .build(),
                    elements3: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            '*': PropertiesOverriddenMapping.builder()
                                .mappingsByPropertyName([
                                    field1: PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([
                                            new NullAssignExpression(modelFromClass(String))
                                        ])
                                        .build()
                                ])
                                .build()
                        ])
                        .build(),
                ])
                .build())
            .build(),
            MapperConfiguration.builder()
                .name("mapElements1")
                .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
                .targetMetaModel(modelFromClass(CollectionElement))
                .build(),
            MapperConfiguration.builder()
                .name("mapElements2")
                .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
                .targetMetaModel(modelFromClass(CollectionElement))
                .build(),
            MapperConfiguration.builder()
                .name("mapFromOtherElement")
                .sourceMetaModel(modelFromClass(CollectionElementOther))
                .targetMetaModel(modelFromClass(CollectionElement))
                .build()
        )                                                                                                                                      |
            "resolve_in_collection_mapping_conflict"

        // test for resolve conflict when are two inner method for mapping list elements by override with name
        //  of mapping method with path like
        //  'elements1=someOneElement.eachMapBy(mapElements1)'
        //  'elements1=elements1.eachMapBy(mapElements1)'
        //  'elements1=someOneElement2.eachMapBy(mapElements1)'
        //  and second one for 'elements2.*'
        //  mapping like 'elements3.*.field1=null'
        OTHER_WITH_ELEMENTS_MODEL              | modelFromClass(OtherWithElements)             | withMapperConfigurations(MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .mappingsByPropertyName([
                    elements1: PropertiesOverriddenMapping.builder()
                        .valueMappingStrategy([
                            new EachElementMapByMethodAssignExpression("mapElements1",
                                createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "someOneElement")),
                            new EachElementMapByMethodAssignExpression("mapElements1",
                                createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "elements1")),
                            new MethodInCurrentClassAssignExpression("mapFromOtherElement",
                                [createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "someOneElement2")],
                                modelFromClass(CollectionElement))
                        ])
                        .build(),
                    elements2: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            '*': PropertiesOverriddenMapping.builder()
                                .valueMappingStrategy([
                                    new MethodInCurrentClassAssignExpression("mapElements2",
                                        List.of(new RawJavaCodeAssignExpression(COLLECTION_ELEMENT_MODEL, "sourceObject")),
                                        modelFromClass(CollectionElement))
                                ])
                                .build()
                        ])
                        .build(),
                    elements3: PropertiesOverriddenMapping.builder()
                        .mappingsByPropertyName([
                            '*': PropertiesOverriddenMapping.builder()
                                .mappingsByPropertyName([
                                    field1: PropertiesOverriddenMapping.builder()
                                        .valueMappingStrategy([
                                            new NullAssignExpression(modelFromClass(String))
                                        ])
                                        .build()
                                ])
                                .build()
                        ])
                        .build(),
                ])
                .build())
            .build(),
            MapperConfiguration.builder()
                .name("mapElements1")
                .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
                .targetMetaModel(modelFromClass(CollectionElement))
                .build(),
            MapperConfiguration.builder()
                .name("mapElements2")
                .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
                .targetMetaModel(modelFromClass(CollectionElement))
                .build(),
            MapperConfiguration.builder()
                .name("mapFromOtherElement")
                .sourceMetaModel(modelFromClass(CollectionElementOther))
                .targetMetaModel(modelFromClass(CollectionElement))
                .build()
        )                                                                                                                                      |
            "resolve_in_collection_mapping_conflict2"

        // mapping Set<elementModel> to List<CollectionElement> in main method
        ClassMetaModel.builder()
            .realClass(List)
            .genericTypes([COLLECTION_ELEMENT_MODEL])
            .build()                           |

            ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([modelFromClass(CollectionElement)])
                .build()                                                                       | EMPTY_CONFIG                                  |
            "main_method_returns_set_when_source_is_list"

        // mapping of List<List<elementModel> to List<Set<CollectionElement>> in main method
        ClassMetaModel.builder()
            .realClass(List)
            .genericTypes([ClassMetaModel.builder()
                               .realClass(List)
                               .genericTypes([COLLECTION_ELEMENT_MODEL])
                               .build()])
            .build()                           |

            ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([ClassMetaModel.builder()
                                   .realClass(List)
                                   .genericTypes([modelFromClass(CollectionElement)])
                                   .build()])
                .build()                                                                       | EMPTY_CONFIG                                  |
            "main_method_returns_set_of_lists_when_source_is_list_of_list"

        // mapping of List<List<CollectionElement>> to Set<NestedCollectionElement[]> inside of other object
        modelFromClass(ListAsListGenericType1) | modelFromClass(ListAsListGenericType2)        | EMPTY_CONFIG                                  |
            "mapping_collections_as_generic_type_in_object"
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
        mapperGenerator.generateMapperCode(mapperGenerateConf)

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
        mapperGenerator.generateMapperCode(mapperGenerateConf)

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
        mapperGenerator.generateMapperCode(mapperGenerateConf)

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
        mapperGenerator.generateMapperCode(mapperGenerateConf)

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
        mapperGenerator.generateMapperCode(mapperGenerateConf)

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
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperGenerateConf),
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
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperGenerateConf),
            sourceClassMetaModel, targetClassMetaModel)

        then:
        MapperGenerationException ex = thrown()
        ex.messagePlaceholders.size() == 3

        def messagesIterator = getSortedMessagesIterator(ex)

        messagesIterator.next() == createMessagePlaceholder("mapper.converter.not.found.between.metamodels",
            "otherModel", "java.lang.String", "someText")
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
                    someText : PropertiesOverriddenMapping.builder()
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
        saveMapperCodeToFile(mapperGenerator.generateMapperCode(mapperGenerateConf),
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

    private static SOME_ENUM1_METAMODEL = createValidEnumMetaModel("someEnum1Model",
        "VAL1", "VAL2", "VAL3", "OTH", "UNKNOWN"
    )

    private static SOME_ENUM2_METAMODEL = createValidEnumMetaModel("someEnum2Model",
        "VAL1", "VAL2", "OTH1", "UNKNOWN"
    )

    private static SOME_ENUM3_METAMODEL = createValidEnumMetaModel("someEnum3Model",
        "VAL1", "VAL2", "VAR3"
    )

    private static METAMODEL_WITH_ENUMS1 = ClassMetaModel.builder()
        .name("metamodelWithEnums1")
        .fields([
            createValidFieldMetaModel("enum1", modelFromClass(SomeEnum1)),
            createValidFieldMetaModel("enum2", modelFromClass(SomeEnum1)),
            createValidFieldMetaModel("enum3", SOME_ENUM2_METAMODEL),
            createValidFieldMetaModel("enum4", SOME_ENUM1_METAMODEL),
            createValidFieldMetaModel("stringToMetamodelOfEnum", modelFromClass(String)),
            createValidFieldMetaModel("metamodelOfEnumToString", SOME_ENUM2_METAMODEL),
            createValidFieldMetaModel("stringToEnumByNativeSpringConversions", modelFromClass(String)),
            createValidFieldMetaModel("stringToEnumByCrudWizardServiceConversion", modelFromClass(String)),
            createValidFieldMetaModel("stringToEnumMetaModelByCrudWizardServiceConversion", modelFromClass(String)),
        ])
        .build()

    private static METAMODEL_WITH_ENUMS2 = ClassMetaModel.builder()
        .name("metamodelWithEnums2")
        .fields([
            createValidFieldMetaModel("enum1", SOME_ENUM1_METAMODEL),
            createValidFieldMetaModel("enum2", modelFromClass(SomeEnum2)),
            createValidFieldMetaModel("enum3", modelFromClass(SomeEnum2)),
            createValidFieldMetaModel("enum4", SOME_ENUM2_METAMODEL),
            createValidFieldMetaModel("stringToMetamodelOfEnum", SOME_ENUM2_METAMODEL),
            createValidFieldMetaModel("metamodelOfEnumToString", modelFromClass(String)),
            createValidFieldMetaModel("stringToEnumByNativeSpringConversions", modelFromClass(SomeEnum1)),
            createValidFieldMetaModel("stringToEnumByCrudWizardServiceConversion", modelFromClass(SomeEnum2)),
            createValidFieldMetaModel("stringToEnumMetaModelByCrudWizardServiceConversion", SOME_ENUM3_METAMODEL),
        ])
        .build()

    private static METAMODEL_WITH_ENUMS3 = ClassMetaModel.builder()
        .name("metamodelWithEnums2")
        .fields([
            createValidFieldMetaModel("spec1enum", SOME_ENUM1_METAMODEL),
            createValidFieldMetaModel("spec2enum", SOME_ENUM1_METAMODEL),
        ])
        .build()

    private static METAMODEL_WITH_ENUMS4 = ClassMetaModel.builder()
        .name("metamodelWithEnums2")
        .fields([
            createValidFieldMetaModel("spec1enum", modelFromClass(SomeEnum3)),
            createValidFieldMetaModel("spec2enum", modelFromClass(SomeEnum3)),
        ])
        .build()

    private static OTHER_WITH_ELEMENTS_MODEL = ClassMetaModel.builder()
        .name("otherWithElementsModel")
        .fields([
            createValidFieldMetaModel("elements1", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("elements2", ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("elements3", ClassMetaModel.builder()
                .realClass(Set)
                .genericTypes([COLLECTION_ELEMENT_MODEL])
                .build()),
            createValidFieldMetaModel("someOneElement", COLLECTION_ELEMENT_MODEL),
            createValidFieldMetaModel("someOneElement2", modelFromClass(CollectionElementOther)),
        ])
        .build()


    private static INSIDE_COLLECTION_ELEMENT = ClassMetaModel.builder()
        .name("INSIDE_COLLECTION_ELEMENT")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("textId", String),
            createValidFieldMetaModel("modificationDateTime", LocalDateTime)
        ])
        .build()

    private static NESTED_NOT_FOUND_MODEL = ClassMetaModel.builder()
        .name("NESTED_NOT_FOUND_MODEL")
        .fields([
            createValidFieldMetaModel("surname", String),
            createValidFieldMetaModel("localDateSome", LocalDate),
        ])
        .build()

    private static OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL = ClassMetaModel.builder()
        .name("OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL")
        .fields([
            createValidFieldMetaModel("someText", String),
            createValidFieldMetaModel("otherLong", Long),
            createValidFieldMetaModel("someObject", NESTED_NOT_FOUND_MODEL),
            createValidFieldMetaModel("list", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([INSIDE_COLLECTION_ELEMENT])
                .build()),
            createValidFieldMetaModel("listOfList", ClassMetaModel.builder()
                .realClass(List)
                .genericTypes([INSIDE_COLLECTION_ELEMENT])
                .build())
        ])
        .build()

    private static EXAMPLE_ENUM_MODEL = createValidEnumMetaModel("exampleEnum",
        "ONE", "TWO", "UNKNOWN")

    private static OTHER_OBJECT_WITH_ENUM_MODEL = ClassMetaModel.builder()
        .name("OTHER_OBJECT_WITH_ENUM")
        .fields([
            createValidFieldMetaModel("otherEnum", EXAMPLE_ENUM_MODEL)
        ])
        .build()

    private static MODEL_WITH_ENUM = ClassMetaModel.builder()
        .name("MODEL_WITH_ENUM")
        .fields([
            createValidFieldMetaModel("someEnum", EXAMPLE_ENUM_MODEL),
            createValidFieldMetaModel("otherObjectWithEnum", OTHER_OBJECT_WITH_ENUM_MODEL),
        ])
        .build()

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

    private static void saveMapperCodeToFile(String code, ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        def folderPath = "target/generated-test-sources/mappers/pl/jalokim/crudwizard/generated/mapper"
        FileUtils.createDirectories(folderPath)
        def mapperClassName = String.format("%sTo%sMapper",
            getClassModelInfoForGeneratedCode(sourceMetaModel),
            getClassModelInfoForGeneratedCode(targetMetaModel)
        )
        FileUtils.writeToFile(String.format("%s/%s.java", folderPath, mapperClassName), code)
    }
}
