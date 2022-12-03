package pl.jalokim.crudwizard.genericapp.mapper.generete.config

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.getPropertiesOverriddenMapping
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.STRING_MODEL

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.sample.ForTestMappingMultiSource2Dto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel

class PropertiesOverriddenMappingResolverIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private PropertiesOverriddenMappingResolver testCase

    @Autowired
    private MetaModelContextService metaModelContextService

    def "populate mapping entries as expected"() {
        given:
        def metaModelContext = metaModelContextService.getMetaModelContext()
        metaModelContext.getMapperMetaModels().setMapperModelWithName("fromLocalDateToLongMapper",
            MapperMetaModel.builder()
            .mapperName("fromLocalDateToLongMapper")
            .sourceClassMetaModel(createClassMetaModelFromClass(LocalDateTime))
            .targetClassMetaModel(createClassMetaModelFromClass(Long))
            .build())

        metaModelContext.getMapperMetaModels().setMapperModelWithName("otherMapper",
            MapperMetaModel.builder()
            .mapperName("otherMapper")
            .sourceClassMetaModel(createClassMetaModelFromClass(String))
            .targetClassMetaModel(createClassMetaModelFromClass(String))
            .build())

        metaModelContext.getClassMetaModels().put(1, PERSONAL_DATA_MODEL)

        MapperGenerateConfiguration mapperGenerateConfiguration = createMapperGenerateConfiguration()

        when:
        testCase.populateMappingEntriesToConfiguration(mapperGenerateConfiguration,
            [
                new MappingEntryModel("firstPerson",
                    "@dummyService.mapPersonPart1(@dummyService.fetchSomeMap(someObject.personPart2.id),  someObject.personPart1.someString)"),

                new MappingEntryModel("secondPerson", ""),
                new MappingEntryModel("secondPerson", "otherPersonPart2"),
                new MappingEntryModel("secondPerson", "otherPersonPart3"),

                new MappingEntryModel("docHolder.document", "documentDataPart1"),
                new MappingEntryModel("docHolder.document", "documentDataPart2"),
                new MappingEntryModel("docHolder.document.id", "documentDataPart2.mainDocId"),

                new MappingEntryModel("sameMappingLikeDocHolder.document", "documentDataPart1"),
                new MappingEntryModel("sameMappingLikeDocHolder.document", "documentDataPart2"),
                new MappingEntryModel("sameMappingLikeDocHolder.document.id", "documentDataPart2.mainDocId"),

                new MappingEntryModel("otherMappingForDocHolder.document", "documentDataPart1"),
                new MappingEntryModel("otherMappingForDocHolder.document", "documentDataPart2"),
                new MappingEntryModel("otherMappingForDocHolder.document.id",
                    "@fromLocalDateToLongMapper(documentDataPart1.localDateTime66)"),

                new MappingEntryModel("otherObject1", "#otherObject2Method(\$rootSourceObject.otherObject1)"),

                new MappingEntryModel("otherObject2.field1", "\$headers['field1']"),
                new MappingEntryModel("otherObject2.someLong", "\$pathVariables ['someLong']"),
                new MappingEntryModel("otherObject2.someInt", "\$requestParams  ['someInt']"),
                new MappingEntryModel("otherObject2.someLocalDateTime",
                    "((m_personaDataModel) \$mappingContext['personalData']).exampleLocalDateTime"),

                new MappingEntryModel("someNumber",
                    "j((Long) pathVariables.get(\"someLong\") + (Long) requestParams.get(\"anotherLong\"))"),
            ],
            [
                otherMapperMethod : [
                    new MappingEntryModel("id", "entryId"),
                    new MappingEntryModel("name", "@otherMapper( \$sourceObject.fullName )"),
                    new MappingEntryModel("", "someObjectWithTimes"),
                ],
                otherObject2Method: [
                    new MappingEntryModel("someLocalDateTime", "someLocalDateTimeAsText"),
                    new MappingEntryModel("someNumberValue", "\$rootSourceObject.otherPersonPart3.field3"),
                ]
            ])

        def multiSourceExampleModel = mapperGenerateConfiguration.getRootConfiguration().getSourceMetaModel()
        def someObjectField = multiSourceExampleModel.getRequiredFieldByName("someObject")

        then:
        def rootMapperConf = mapperGenerateConfiguration.getRootConfiguration()

        verifyAll(rootMapperConf.getPropertyOverriddenMapping()) {
            valueMappingStrategy == []
            mappingsByPropertyName.keySet() == ["firstPerson", "secondPerson", "docHolder",
                                                "sameMappingLikeDocHolder", "otherMappingForDocHolder",
                                                "otherObject1", "otherObject2", "someNumber"] as Set
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "firstPerson")) {
            valueMappingStrategy == [
                new BySpringBeanMethodAssignExpression(DummyService, "dummyService", "mapPersonPart1",
                    [new BySpringBeanMethodAssignExpression(DummyService, "dummyService", "fetchSomeMap",
                        [
                            new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject", [someObjectField])
                                .createExpressionWithNextField("personPart2", READ_FIELD_RESOLVER_CONFIG)
                                .createExpressionWithNextField("id", READ_FIELD_RESOLVER_CONFIG)
                        ]),
                     new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject", [someObjectField])
                         .createExpressionWithNextField("personPart1", READ_FIELD_RESOLVER_CONFIG)
                         .createExpressionWithNextField("someString", READ_FIELD_RESOLVER_CONFIG)
                    ])
            ]
            mappingsByPropertyName.isEmpty()
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "secondPerson")) {
            valueMappingStrategy == [
                new RawJavaCodeAssignExpression(multiSourceExampleModel, "sourceObject"),
                new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                    [multiSourceExampleModel.getRequiredFieldByName("otherPersonPart2")]),
                new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                    [multiSourceExampleModel.getRequiredFieldByName("otherPersonPart3")])
            ]
            mappingsByPropertyName.isEmpty()
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "docHolder")) {
            valueMappingStrategy.isEmpty()
            mappingsByPropertyName.keySet() == ["document"] as Set
            verifyAll(mappingsByPropertyName.get("document")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart1")]),
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart2")]),
                ]
                mappingsByPropertyName.keySet() == ["id"] as Set
                verifyAll(mappingsByPropertyName.get("id")) {
                    valueMappingStrategy == [
                        new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                            [multiSourceExampleModel.getRequiredFieldByName("documentDataPart2")])
                            .createExpressionWithNextField("mainDocId", READ_FIELD_RESOLVER_CONFIG)
                    ]
                    mappingsByPropertyName.isEmpty()
                }
            }
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "otherMappingForDocHolder")) {
            valueMappingStrategy.isEmpty()
            mappingsByPropertyName.keySet() == ["document"] as Set
            verifyAll(mappingsByPropertyName.get("document")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart1")]),
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart2")])
                ]
                mappingsByPropertyName.keySet() == ["id"] as Set
                verifyAll(mappingsByPropertyName.get("id")) {
                    valueMappingStrategy == [
                        new ByMapperNameAssignExpression(createClassMetaModelFromClass(Long),
                            new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                                [multiSourceExampleModel.getRequiredFieldByName("documentDataPart1")])
                                .createExpressionWithNextField("localDateTime66", READ_FIELD_RESOLVER_CONFIG),
                            "fromLocalDateToLongMapper"
                        )
                    ]
                    mappingsByPropertyName.isEmpty()
                }
            }
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "sameMappingLikeDocHolder")) {
            valueMappingStrategy.isEmpty()
            mappingsByPropertyName.keySet() == ["document"] as Set
            verifyAll(mappingsByPropertyName.get("document")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart1")]),
                    new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                        [multiSourceExampleModel.getRequiredFieldByName("documentDataPart2")])
                ]
                mappingsByPropertyName.keySet() == ["id"] as Set
                verifyAll(mappingsByPropertyName.get("id")) {
                    valueMappingStrategy == [
                        new FieldsChainToAssignExpression(multiSourceExampleModel, "sourceObject",
                            [multiSourceExampleModel.getRequiredFieldByName("documentDataPart2")])
                            .createExpressionWithNextField("mainDocId", READ_FIELD_RESOLVER_CONFIG),
                    ]
                    mappingsByPropertyName.keySet().isEmpty()
                }
            }
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "otherObject1")) {
            valueMappingStrategy == [new MethodInCurrentClassAssignExpression(
                "otherObject2Method",
                [
                    new FieldsChainToAssignExpression(multiSourceExampleModel,
                        "rootSourceObject", [multiSourceExampleModel.getRequiredFieldByName("otherObject1")])
                ],
                createClassMetaModelFromClass(ForTestMappingMultiSource2Dto.OtherObject))]
            mappingsByPropertyName.isEmpty()
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "otherObject2")) {
            valueMappingStrategy.isEmpty()
            mappingsByPropertyName.keySet() == ["field1", "someLong", "someInt", "someLocalDateTime"] as Set

            verifyAll(mappingsByPropertyName.get("field1")) {
                valueMappingStrategy == [new RawJavaCodeAssignExpression(STRING_MODEL, "headers.get(\"field1\")")]
                mappingsByPropertyName.keySet().isEmpty()
            }

            verifyAll(mappingsByPropertyName.get("someLong")) {
                valueMappingStrategy == [new FieldsChainToAssignExpression(PATH_VARIABLES_MODEL,
                    "pathVariables", [PATH_VARIABLES_MODEL.getRequiredFieldByName("someLong")])]
                mappingsByPropertyName.keySet().isEmpty()
            }

            verifyAll(mappingsByPropertyName.get("someInt")) {
                valueMappingStrategy == [new FieldsChainToAssignExpression(REQUEST_PARAMS_MODEL,
                    "requestParams", [REQUEST_PARAMS_MODEL.getRequiredFieldByName("someInt")])]
                mappingsByPropertyName.keySet().isEmpty()
            }

            verifyAll(mappingsByPropertyName.get("someLocalDateTime")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(PERSONAL_DATA_MODEL,
                        "((java.util.Map<java.lang.String, java.lang.Object>) mappingContext.get(\"personalData\"))",
                        [PERSONAL_DATA_MODEL.getRequiredFieldByName("exampleLocalDateTime")])
                ]
                mappingsByPropertyName.keySet().isEmpty()
            }
        }

        verifyAll(getPropertiesOverriddenMapping(rootMapperConf.getPropertyOverriddenMapping(), "someNumber")) {
            valueMappingStrategy == [
                new RawJavaCodeAssignExpression(
                    createClassMetaModelFromClass(Long),
                    "(Long) pathVariables.get(\"someLong\") + (Long) requestParams.get(\"anotherLong\")"
                )
            ]
            mappingsByPropertyName.keySet().isEmpty()
        }

        verifyAll(mapperGenerateConfiguration.getMapperConfigurationByMethodName("otherMapperMethod").getPropertyOverriddenMapping()) {
            valueMappingStrategy == [
                new FieldsChainToAssignExpression(SOME_ENTRY_MODEL,
                    "sourceObject", [SOME_ENTRY_MODEL.getRequiredFieldByName("someObjectWithTimes")])]
            mappingsByPropertyName.keySet() == ["id", "name"] as Set

            verifyAll(mappingsByPropertyName.get("id")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(SOME_ENTRY_MODEL,
                        "sourceObject", [SOME_ENTRY_MODEL.getRequiredFieldByName("entryId")])
                ]
                mappingsByPropertyName.keySet().isEmpty()
            }

            verifyAll(mappingsByPropertyName.get("name")) {
                valueMappingStrategy == [
                    new ByMapperNameAssignExpression(createClassMetaModelFromClass(String),
                        new FieldsChainToAssignExpression(SOME_ENTRY_MODEL,
                            "sourceObject", [SOME_ENTRY_MODEL.getRequiredFieldByName("fullName")]),
                        "otherMapper"
                    )
                ]
                mappingsByPropertyName.keySet().isEmpty()
            }
        }

        verifyAll(mapperGenerateConfiguration.getMapperConfigurationByMethodName("otherObject2Method").getPropertyOverriddenMapping()) {
            valueMappingStrategy.isEmpty()
            mappingsByPropertyName.keySet() == ["someLocalDateTime", "someNumberValue"] as Set

            verifyAll(mappingsByPropertyName.get("someLocalDateTime")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(OTHER_OBJECT_MODEL,
                        "sourceObject", [OTHER_OBJECT_MODEL.getRequiredFieldByName("someLocalDateTimeAsText")])
                ]
                mappingsByPropertyName.keySet().isEmpty()
            }

            verifyAll(mappingsByPropertyName.get("someNumberValue")) {
                valueMappingStrategy == [
                    new FieldsChainToAssignExpression(multiSourceExampleModel,
                        "rootSourceObject", [multiSourceExampleModel.getRequiredFieldByName("otherPersonPart3")])
                        .createExpressionWithNextField("field3", READ_FIELD_RESOLVER_CONFIG)
                ]
                mappingsByPropertyName.keySet().isEmpty()
            }
        }
    }

    MapperGenerateConfiguration createMapperGenerateConfiguration() {
        def generateConfiguration = MapperGenerateConfiguration.builder()
            .rootConfiguration(MapperConfiguration.builder()
                .sourceMetaModel(multiSourceExampleModel())
                .targetMetaModel(createClassMetaModelFromClass(ForTestMappingMultiSource2Dto))
                .build())
            .pathVariablesClassModel(PATH_VARIABLES_MODEL)
            .requestParamsClassModel(REQUEST_PARAMS_MODEL)
            .build()

        generateConfiguration.addSubMapperConfiguration("otherMapperMethod", MapperConfiguration.builder()
            .name("otherMapperMethod")
            .sourceMetaModel(SOME_ENTRY_MODEL)
            .targetMetaModel(createClassMetaModelFromClass(ForTestMappingMultiSource2Dto.SomeEntryDto))
            .build())

        generateConfiguration.addSubMapperConfiguration("otherObject2Method", MapperConfiguration.builder()
            .name("otherObject2Method")
            .sourceMetaModel(OTHER_OBJECT_MODEL)
            .targetMetaModel(createClassMetaModelFromClass(ForTestMappingMultiSource2Dto.OtherObject))
            .build())

        generateConfiguration
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

        def otherObjectModel = ClassMetaModel.builder()
            .name("otherObjectModel")
            .fields([
                createValidFieldMetaModel("field1", String),
                createValidFieldMetaModel("someLong", Long),
                createValidFieldMetaModel("someInt", Long),
                createValidFieldMetaModel("someLocalDateTimeAsText", String),
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
                        createValidFieldMetaModel("field66", Long),
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
                createValidFieldMetaModel("otherObject1", otherObjectModel),
                createValidFieldMetaModel("otherObject2", otherObjectModel),
            ])
            .build()
    }

    private static final PATH_VARIABLES_MODEL = ClassMetaModel.builder()
        .name("pathVariablesModel")
        .fields([
            createValidFieldMetaModel("someLong", Integer)
        ])
        .build()

    private static final REQUEST_PARAMS_MODEL = ClassMetaModel.builder()
        .name("requestParamsModel")
        .fields([
            createValidFieldMetaModel("someInt", Integer)
        ])
        .build()

    private static final PERSONAL_DATA_MODEL = ClassMetaModel.builder()
        .name("personaDataModel")
        .fields([
            createValidFieldMetaModel("exampleLocalDateTime", String)
        ])
        .build()

    private static final SOME_ENTRY_MODEL = ClassMetaModel.builder()
        .name("someEntryModel")
        .fields([
            createValidFieldMetaModel("entryId", String),
            createValidFieldMetaModel("fullName", String),
            createValidFieldMetaModel("someObjectWithTimes", ClassMetaModel.builder()
                .name("someObjectWithTimesModel")
                .fields([
                    createValidFieldMetaModel("somePeriod", Period),
                    createValidFieldMetaModel("someLocaleDate", LocalDate),
                    createValidFieldMetaModel("someInt", Integer),
                ])
                .build())
        ])
        .build()

    private static final OTHER_OBJECT_MODEL = ClassMetaModel.builder()
        .name("otherObjectModel")
        .fields([
            createValidFieldMetaModel("field1", String),
            createValidFieldMetaModel("someLong", Long),
            createValidFieldMetaModel("someInt", Integer),
            createValidFieldMetaModel("someLocalDateTimeAsText", String),
        ])
        .build()
}
