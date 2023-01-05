package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidEnumMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.sample.ClassHasSamplePersonDto
import pl.jalokim.crudwizard.core.sample.ForTestMappingMultiSourceDto
import pl.jalokim.crudwizard.core.sample.FromStringToObject
import pl.jalokim.crudwizard.core.sample.InnerDocumentDto
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SamplePersonDtoWitOtherObject
import pl.jalokim.crudwizard.core.sample.SomeDocumentDto
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSimpleSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeObjectWithFewObjects
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElementOther
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ListAsListGenericType2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.MappingCollections
import pl.jalokim.crudwizard.genericapp.mapper.conversion.NestedElementObject
import pl.jalokim.crudwizard.genericapp.mapper.conversion.OtherElementCollection
import pl.jalokim.crudwizard.genericapp.mapper.conversion.OtherWithElements
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeContact1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeDocument1Entity
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum2
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum3
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomePerson1
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
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
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService

class MapperCodeGeneratorSamples {

    public static final MapperGenerateConfiguration EMPTY_CONFIG = MapperGenerateConfiguration.builder()
        .rootConfiguration(MapperConfiguration.builder().build())
        .build()

    static final MappingCollections MAPPING_COLLECTIONS_AS_CLASS = new MappingCollections(
        ["1", "2"],
        [1L, 3L],
        [
            new CollectionElement(
                "field1",
                "field2",
                33L,
                new NestedElementObject(
                    name: "nameValue",
                    surname: "surnameValue",
                )
            ),
            new CollectionElement(
                "field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ] as CollectionElement[],
        [
            new CollectionElement(
                "arrayList2_field1",
                "field2",
                33L,
                new NestedElementObject(
                    name: "nameValue",
                    surname: "surnameValue",
                )
            ),
            new CollectionElement(
                "arrayList2_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ] as CollectionElement[],
        [
            new CollectionElement(
                "arraySet_field1",
                "field2",
                33L,
                new NestedElementObject(
                    name: "nameValue",
                    surname: "surnameValue",
                )
            ),
            new CollectionElement(
                "arraySet_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ] as CollectionElement[],
        [
            new CollectionElement(
                "listList_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ],
        [
            new CollectionElement(
                "setSet_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ] as Set,
        [
            "1": new CollectionElement(
                "map1_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            "2": new CollectionElement(
                "map2_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ],
        [
            new OtherElementCollection(
                "othElemField1Value",
                "othElemFieldValue2"
            ),
            new OtherElementCollection(
                "othElemField1Value_1",
                "othElemFieldValue2_2"
            )
        ],
        new CollectionElement(
            "someOneField1_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        ),
        new CollectionElement(
            "someOneField2_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        )
    )

    static final MAPPING_COLLECTIONS_AS_MODEL = [
        strings                          : ["1", "2"] as Set,
        longs                            : ["1", "3"] as String[],
        arrayList                        : [
            [
                field1    : "field1",
                field2    : "field2",
                field3    : 33L,
                someObject: [
                    name   : "nameValue",
                    surname: "surnameValue",
                ],
            ],
            [
                field1    : "field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ],
        ],
        arrayList2                       : [
            [
                field1    : "arrayList2_field1",
                field2    : "field2",
                field3    : 33L,
                someObject: [
                    name   : "nameValue",
                    surname: "surnameValue",
                ],
            ],
            [
                field1    : "arrayList2_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ],
        ],
        arraySet                         : [
            [
                field1    : "arraySet_field1",
                field2    : "field2",
                field3    : 33L,
                someObject: [
                    name   : "nameValue",
                    surname: "surnameValue",
                ],
            ],
            [
                field1    : "arraySet_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ],
        ] as Set,
        listList                         : [
            [
                field1    : "listList_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ]
        ],
        setSet                           : [
            [
                field1    : "setSet_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ]
        ] as Set,
        someMap                          : [
            1L: [
                field1    : "map1_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ],
            2L: [
                field1    : "map2_field11",
                field2    : "field22",
                field3    : 44L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ]
        ],
        mappedListElementByProvidedMethod: [
            [
                othElemField1: "othElemField1Value",
                othElemField2: "othElemFieldValue2",
            ],
            [
                othElemField1: "othElemField1Value_1",
                othElemField2: "othElemFieldValue2_2",
            ]
        ],
        someOneField1                    : [
            field1    : "someOneField1_field1",
            field2    : "field22",
            field3    : 44L,
            someObject: [
                name   : "nameValue2",
                surname: "surnameValue2",
            ],
        ],
        someOneField2                    : [
            field1    : "someOneField2_field1",
            field2    : "field22",
            field3    : 44L,
            someObject: [
                name   : "nameValue2",
                surname: "surnameValue2",
            ],
        ],
    ]

    public static final MappingCollections CUSTOM_MAPPING_COLLECTION_CLS = new MappingCollections(
        ["1", "2"],
        [1L, 3L],
        [new CollectionElement(
            "someOneField1_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        )] as CollectionElement[],
        [
            new CollectionElement(
                "someOneField1_field1",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            new CollectionElement(
                "field1",
                "field2",
                33L,
                new NestedElementObject(
                    name: "nameValue",
                    surname: "surnameValue",
                )
            ),
            new CollectionElement(
                "field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            new CollectionElement(
                "arraySet_field1",
                "field2",
                33L,
                new NestedElementObject(
                    name: "nameValue",
                    surname: "surnameValue",
                )
            ),
            new CollectionElement(
                "arraySet_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            new CollectionElement(
                "someOneField2_field1",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ] as CollectionElement[],
        [
            new CollectionElement("someStringValue", "randomText", null, null)
        ] as CollectionElement[],
        [new CollectionElement(
            "someOneField1_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        )],
        [new CollectionElement(
            "someOneField2_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        )] as Set,
        [
            "1": new CollectionElement(
                "map1_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            "2": new CollectionElement(
                "map2_field11",
                "field22",
                44L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            )
        ],
        [
            new OtherElementCollection(
                "othElemField1Value",
                "othElemFieldValue2"
            ),
            new OtherElementCollection(
                "othElemField1Value_1",
                "othElemFieldValue2_2"
            )
        ],
        new CollectionElement(
            "someOneField1_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        ),
        new CollectionElement(
            "someOneField2_field1",
            "field22",
            44L,
            new NestedElementObject(
                name: "nameValue2",
                surname: "surnameValue2",
            )
        )
    )

    public static final SamplePersonDto SAMPLE_PERSON_DTO_AS_CLASS = SamplePersonDto.builder()
        .id(1L)
        .name("someName")
        .surname("surName")
        .birthDay(LocalDate.of(1990, 1, 12))
        .build()

    static final STRING_TO_FROM_STRING_TO_OBJECT_CONF = withMapperConfigurations(
        MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .valueMappingStrategy([
                    new RawJavaCodeAssignExpression(modelFromClass(FromStringToObject),
                        "new " + FromStringToObject.canonicalName + "((String) sourceObject)")
                ])
                .build())
            .build())

    static final FROM_STRING_TO_OBJECT_CONF_TO_STRING = withMapperConfigurations(
        MapperConfiguration.builder()
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .valueMappingStrategy([
                    createFieldsChainExpression(modelFromClass(FromStringToObject), "sourceText")
                ])
                .build())
            .build())

    public static final PERSON_META_MODEL_AS_METAMODEL = [
        id       : 1L,
        name     : "someName",
        surname  : "surName",
        birthDay : LocalDate.of(1990, 1, 12),
        lastLogin: null
    ]

    public static final GenericMapperArgument CLASS_HAS_SAMPLE_PERSON_MODEL_2_AS_MODEL = emptyGenericMapperArgument([
        someId                  : "18",
        samplePersonDto         : [
            id     : 1L,
            name   : "name1",
            surname: "surname1",
        ],
        otherPersonDto          : [
            id     : 2L,
            name   : "name2",
            surname: "surname2",
        ],
        someObjectWithFewObjects: [
            someDtoWithBuilder: [
                test1            : "test1VAl",
                someLocalDateTime: "2022-05-23T12:00:12",
            ],
            someDtoWithSetters: [
                someString2: "someString2Value",
                surname    : "surnameValue",
                name       : "nameValue",
            ]
        ]
    ])

    public static final GenericMapperArgument MULTI_SOURCE_EXAMPLE_MODEL_AS_MDL = emptyGenericMapperArgument([
        someObject                  : [
            personPart1: [
                someString: "someStringVal",
                name      : "nameVal",
                surname   : "surnameVal",
            ],
            personPart2: [
                someOtherDto: [
                    someString3  : "someString3VAl",
                    someLong3    : 3L,
                    someDataTime3: LocalDateTime.of(2022, 12, 11, 14, 44, 12),
                ],
                id          : "2",
                lastLogin   : LocalDateTime.of(2022, 11, 19, 21, 01, 14),
                birthDay    : LocalDate.of(1990, 10, 13)
            ],
        ],
        otherPersonPart2            : [
            someOtherDto: [
                someString3  : "someString4VAl",
                someLong3    : 4L,
                someDataTime3: LocalDateTime.of(2022, 12, 11, 14, 44, 11),
            ],
            id          : "233",
            lastLogin   : LocalDateTime.of(2022, 11, 19, 21, 01, 15),
            birthDay    : LocalDate.of(1996, 10, 15)
        ],
        otherPersonPart3            : [
            someString : "someString_Val33",
            otherString: "otherString_Val33",
            field3     : "field3_Val33",
        ],
        surname                     : "MainSurname",
        name                        : "MainName",
        documentDataPart1           : [
            documentData   : [
                serialNumber: "serialNumber55",
                signedBy    : "signedBy55",
                field66     : 6655L,
            ],
            localDateTime66: LocalDateTime.of(1966, 12, 12, 11, 12, 56)
        ],
        documentDataPart2           : [
            mainDocId   : 11L,
            documentData: [
                otherString: "otherStringVAlue",
                docHash    : "docHashVAlue",
                docId      : 55L
            ]
        ],
        testCurrentNodeObjectWrapper: [
            testCurrentNodeObject: new ForTestMappingMultiSourceDto.TestCurrentNodeObjectInModel(
                uuid: "uuid1_1"
            ),
            simpleCurrentNodeTest: "uuid1_2"
        ]
    ]
    )

    public static final ForTestMappingMultiSourceDto FOR_TEST_MAPPING_MULTI_SOURCE_DTO_AS_CLS = new ForTestMappingMultiSourceDto(
        new SamplePersonDtoWitOtherObject(
            someOtherDto: new SomeSimpleValueDto(
                "someString3VAl", 3L, LocalDateTime.of(2022, 12, 11, 14, 44, 12)
            ),
            someString: "someStringVal",
            id: 2L,
            lastLogin: LocalDateTime.of(2022, 11, 19, 21, 01, 14),
            birthDay: LocalDate.of(1990, 10, 13),
            name: "nameVal",
            surname: "surnameVal",
        ),
        new SamplePersonDtoWitOtherObject(
            someOtherDto: new SomeSimpleValueDto(
                "someString4VAl", 4L, LocalDateTime.of(2022, 12, 11, 14, 44, 11)
            ),
            someString: "someString_Val33",
            id: 233L,
            lastLogin: LocalDateTime.of(2022, 11, 19, 21, 01, 15),
            birthDay: LocalDate.of(1996, 10, 15),
            name: "MainName",
            surname: "MainSurname",
        ),
        new ForTestMappingMultiSourceDto.DocumentHolderDto(
            new SomeDocumentDto(
                new InnerDocumentDto("serialNumber55",
                    "signedBy55",
                    55L
                ),
                11L
            )
        ),
        new ForTestMappingMultiSourceDto.DocumentHolderDto(
            new SomeDocumentDto(
                new InnerDocumentDto("serialNumber55",
                    "signedBy55",
                    55L
                ),
                11L
            )
        ),
        new ForTestMappingMultiSourceDto.DocumentHolderDto(
            new SomeDocumentDto(
                new InnerDocumentDto("serialNumber55",
                    "signedBy55",
                    55L
                ),
                2013L
            )
        ),
        new ForTestMappingMultiSourceDto.TestCurrentNodeObject(uuid: "uuid1_1"),
        "uuid1_2"
    )

    public static final GenericMapperArgument SOME_PERSON_MODEL1_AS_MDL = emptyGenericMapperArgument([
        id          : 1L,
        name        : "nameVal",
        surname     : "surnameVal",
        passport    : [
            id     : 2L,
            number : 342545L,
            validTo: LocalDate.of(2022, 10, 11)
        ],
        idCard      : [
            id     : 3L,
            number : 56546L,
            validTo: LocalDate.of(2022, 10, 12)
        ],
        phoneContact: [
            type : 1L,
            value: "654-345-651"
        ],
        emailContact: null,
    ])

    public static final SomePerson1 SOME_PERSON_1_AS_CLS = new SomePerson1(
        1L,
        "nameVal",
        "surnameVal",
        new SomeDocument1(
            2L,
            342545L,
            LocalDate.of(2022, 10, 11),
            "nameVal"
        ),
        new SomeDocument1(
            3L,
            56546L,
            null,
            "mappedByIdCardMethod"
        ),
        new SomeContact1(
            1L,
            "654-345-651",
            "nameVal"
        ),
        null
    )
    public static final LinkedHashMap<String, Serializable> SOME_PERSON_MODEL1_AS_MDL1 = [
        id          : 1L,
        name        : "nameVal",
        surname     : "surnameVal",
        passport    : [
            id     : 2L,
            number : 342545L,
            validTo: null
        ],
        idCard      : [
            id     : 3L,
            number : 56546L,
            validTo: null
        ],
        phoneContact: [
            type : 1L,
            value: "654-345-651"
        ],
        emailContact: null,
        someMetaData: null
    ]

    static final ClassMetaModel SOME_DOCUMENT_MODEL1 = ClassMetaModel.builder()
        .name("someDocument1")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("number", Long),
            createValidFieldMetaModel("validTo", LocalDate),
        ])
        .build()

    public static final MapperGenerateConfiguration SOME_PERSON1_FROM_CLS_MDL_CONF = withMapperConfigurations(
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
    )

    public static final LinkedHashMap<String, Serializable> SOME_PERSON_MODEL1_AS_MDL2 = [
        id          : 1L,
        name        : "nameVal",
        surname     : "surnameVal",
        passport    : [
            id     : 2L,
            number : 342545L,
            validTo: LocalDate.of(2022, 10, 11)
        ],
        idCard      : [
            id     : 3L,
            number : 56546L,
            validTo: LocalDate.of(2022, 5, 11)
        ],
        phoneContact: [
            type : 1L,
            value: "654-345-651"
        ],
        emailContact: null,
        someMetaData: null
    ]

    public static final ClassHasSamplePersonDto CLASS_HAS_SAMPLE_PERSON_DTO_AS_CLS = new ClassHasSamplePersonDto(
        SamplePersonDto.builder()
            .id(1L)
            .name("name1")
            .surname("surname1")
            .build(),
        SamplePersonDto.builder()
            .id(2L)
            .name("name2")
            .surname("surname2")
            .build(),
        SomeObjectWithFewObjects.builder()
            .someDtoWithBuilder(
                SomeDtoWithBuilder.builder()
                    .testLong1(15)
                    .test1("test1VAl")
                    .localDateTime1(LocalDateTime.of(2022, 05, 23, 12, 0, 12))
                    .build()
            )
            .someDtoWithSetters(new SomeDtoWithSetters(
                name: "nameValue",
                surname: "surnameValue",
                someLong2: 2L,
                someString2: "someString2Value"
            ))
            .build(),
        18L
    )

    static final ClassMetaModel SOME_CONTACT_MODEL1 = ClassMetaModel.builder()
        .name("someContact1")
        .fields([
            createValidFieldMetaModel("type", Long),
            createValidFieldMetaModel("value", String),
        ])
        .build()

    static final ClassMetaModel SOME_METADATA1 = ClassMetaModel.builder()
        .name("someMetaData1")
        .fields([
            createValidFieldMetaModel("key", Long),
            createValidFieldMetaModel("value", String),
        ])
        .build()

    static final ClassMetaModel SOME_PERSON_MODEL1 = ClassMetaModel.builder()
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

    static final MapperGenerateConfiguration MAPPING_PERSON_1_CONFIG = withMapperConfigurations(
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

    static final MapperGenerateConfiguration SOME_PERSON_MODEL1_FEW_IGNORED = withMapperConfigurations(
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

    static final ClassMetaModel NESTED_ELEMENT_OBJECT_MODEL = ClassMetaModel.builder()
        .name("nestedElementObjectModel")
        .fields([
            createValidFieldMetaModel("name", String),
            createValidFieldMetaModel("surname", String),
        ])
        .build()

    static final ClassMetaModel COLLECTION_ELEMENT_MODEL = ClassMetaModel.builder()
        .name("collectionElementModel")
        .fields([
            createValidFieldMetaModel("field1", String),
            createValidFieldMetaModel("field2", String),
            createValidFieldMetaModel("field3", Long),
            createValidFieldMetaModel("someObject", NESTED_ELEMENT_OBJECT_MODEL),
        ])
        .build()

    static final ClassMetaModel OTHER_ELEMENT_COLLECTION_MODEL = ClassMetaModel.builder()
        .name("otherElementCollectionModel")
        .fields([
            createValidFieldMetaModel("othElemField1", String),
            createValidFieldMetaModel("othElemField2", String),
        ])
        .build()

    static final ClassMetaModel MAPPING_COLLECTIONS_MODEL = ClassMetaModel.builder()
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

    static final SOME_ENUM1_METAMODEL = createValidEnumMetaModel("someEnum1Model",
        "VAL1", "VAL2", "VAL3", "OTH", "UNKNOWN"
    )

    static final SOME_ENUM2_METAMODEL = createValidEnumMetaModel("someEnum2Model",
        "VAL1", "VAL2", "OTH1", "UNKNOWN"
    )

    static final SOME_ENUM3_METAMODEL = createValidEnumMetaModel("someEnum3Model",
        "VAL1", "VAL2", "VAR3"
    )

    static final METAMODEL_WITH_ENUMS1 = ClassMetaModel.builder()
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

    static final METAMODEL_WITH_ENUMS2 = ClassMetaModel.builder()
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

    static final METAMODEL_WITH_ENUMS3 = ClassMetaModel.builder()
        .name("metamodelWithEnums2")
        .fields([
            createValidFieldMetaModel("spec1enum", SOME_ENUM1_METAMODEL),
            createValidFieldMetaModel("spec2enum", SOME_ENUM1_METAMODEL),
        ])
        .build()

    static final METAMODEL_WITH_ENUMS4 = ClassMetaModel.builder()
        .name("metamodelWithEnums2")
        .fields([
            createValidFieldMetaModel("spec1enum", modelFromClass(SomeEnum3)),
            createValidFieldMetaModel("spec2enum", modelFromClass(SomeEnum3)),
        ])
        .build()

    static final OTHER_WITH_ELEMENTS_MODEL = ClassMetaModel.builder()
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

    static final INSIDE_COLLECTION_ELEMENT = ClassMetaModel.builder()
        .name("INSIDE_COLLECTION_ELEMENT")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("textId", String),
            createValidFieldMetaModel("modificationDateTime", LocalDateTime)
        ])
        .build()

    static final NESTED_NOT_FOUND_MODEL = ClassMetaModel.builder()
        .name("NESTED_NOT_FOUND_MODEL")
        .fields([
            createValidFieldMetaModel("surname", String),
            createValidFieldMetaModel("localDateSome", LocalDate),
        ])
        .build()

    static final OBJECT_FOR_NOT_FOND_MAPPINGS_MODEL = ClassMetaModel.builder()
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

    static final EXAMPLE_ENUM_MODEL = createValidEnumMetaModel("exampleEnum",
        "ONE", "TWO", "UNKNOWN")

    static final OTHER_OBJECT_WITH_ENUM_MODEL = ClassMetaModel.builder()
        .name("OTHER_OBJECT_WITH_ENUM")
        .fields([
            createValidFieldMetaModel("otherEnum", EXAMPLE_ENUM_MODEL)
        ])
        .build()

    static final MODEL_WITH_ENUM = ClassMetaModel.builder()
        .name("MODEL_WITH_ENUM")
        .fields([
            createValidFieldMetaModel("someEnum", EXAMPLE_ENUM_MODEL),
            createValidFieldMetaModel("otherObjectWithEnum", OTHER_OBJECT_WITH_ENUM_MODEL),
        ])
        .build()

    public static final DTO_WITH_BUILDER_MDL = [
        test1    : "field1",
        name     : "nameVal",
        testLong1: 11L,
        someId   : 55L,
    ]

    public static final DTO_WITH_SUPER_BUILDER_MDL = [
        someString1     : "123",
        someLong1       : 11L,
        superStringField: "superStringFieldVal",
        localDateTime1  : LocalDateTime.of(2022, 11, 17, 22, 04, 10),
    ]

    public static final SOME_DTO_WITH_SETTERS_MDL = [
        someString2: "someString2",
        someLong2  : 12L,
        id         : 1,
        name       : "name",
        surname    : "surname",
        birthDay   : LocalDateTime.of(LocalDate.of(1990, 12, 12), LocalTime.of(12, 13)),
        lastLogin  : LocalDateTime.of(LocalDate.of(2022, 11, 12), LocalTime.of(19, 59)),
    ]

    public static final SomeDtoWithSetters SOME_DTO_WITH_SETTERS_CLS = new SomeDtoWithSetters(
        someString2: "someString2",
        someLong2: 12L,
        id: 1,
        name: "name",
        surname: "surname",
        birthDay: LocalDate.of(1990, 12, 12),
        lastLogin: LocalDateTime.of(LocalDate.of(2022, 11, 12), LocalTime.of(19, 59)),
    )

    public static final HAS_SAMPLE_PERSON_MDL_1 = [
        someId         : "13",
        samplePersonDto: [
            personId  : 12L,
            personName: "person1"
        ],
        otherPersonDto : [
            personId  : 14L,
            personName: "person2"
        ]
    ]

    public static final ClassHasSamplePersonDto HAS_SAMPLE_PERSON_CLS_1 = new ClassHasSamplePersonDto(
        SamplePersonDto.builder()
            .id(12L)
            .name("person1")
            .build(),
        SamplePersonDto.builder()
            .id(14L)
            .name("person2")
            .build(),
        null,
        13L
    )

    public static final MapperGenerateConfiguration HAS_SAMPLE_PERSON_MODEL_1_CONF = withMapperConfigurations(MapperConfiguration.builder()
        .propertyOverriddenMapping(PropertiesOverriddenMapping.builder().ignoredFields(["someObjectWithFewObjects"]).build())
        .build()
    )

    public static final MapperGenerateConfiguration CUSTOM_MAPPING_COLLECTION_MDL_CLS = withMapperConfigurations(MapperConfiguration.builder()
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
    )

    static final SomeDtoWithSimpleSuperBuilder DTO_WITH_SUPER_BUILDER_CLS = SomeDtoWithSimpleSuperBuilder.builder()
        .someString1("123")
        .someLong1(11L)
        .superStringField("superStringFieldVal")
        .localDateTime1(LocalDateTime.of(2022, 11, 17, 22, 04, 10))
        .build()

    static final SomeDtoWithBuilder DTO_WITH_BUILDER_CLS = SomeDtoWithBuilder.builder()
        .test1("field1")
        .testLong1(11L)
        .build()

    static MapperGenerateConfiguration withMapperConfigurations(MapperConfiguration rootConfiguration, MapperConfiguration... subMappersConfiguration) {
        def mapperGenerateConfiguration = EMPTY_CONFIG.toBuilder()
            .rootConfiguration(rootConfiguration)
            .build()

        subMappersConfiguration.each {
            mapperGenerateConfiguration.addSubMapperConfiguration(it.name, it)
        }

        mapperGenerateConfiguration
    }

    static ClassMetaModel modelFromClass(Class<?> someClass,
        FieldMetaResolverConfiguration fieldMetaResolverConfiguration = WRITE_FIELD_RESOLVER_CONFIG) {
        ClassMetaModelFactory.createNotGenericClassMetaModel(createClassMetaModelFromClass(someClass), fieldMetaResolverConfiguration)
    }

    static GenericMapperArgument emptyGenericMapperArgument(Object sourceObject = 1) {
        GenericMapperArgument.builder()
            .sourceObject(sourceObject)
            .headers([:])
            .pathVariables([:])
            .requestParams([:])
            .mappingContext([:])
            .build()
    }

    static FieldsChainToAssignExpression createFieldsChainExpression(ClassMetaModel sourceMetaModel,
        String fieldName, String variableName = "sourceObject") {
        new FieldsChainToAssignExpression(sourceMetaModel, variableName, [
            sourceMetaModel.getRequiredFieldByName(fieldName)
        ])
    }

    public static final MapperGenerateConfiguration SOME_CLS_TO_CLS_CONF = withMapperConfigurations(MapperConfiguration.builder()
        .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName([
                someLocalDate: PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([createFieldsChainExpression(modelFromClass(SomeDocument1), "validToDate")])
                    .build()
            ])
            .build())
        .build())

    public static final GenericMapperArgument SOME_DOC_1_CLS = emptyGenericMapperArgument(new SomeDocument1(
        1L, 2L, LocalDate.of(2022, 12, 1), "text"))

    public static final SomeDocument1Entity SOME_DOC_EN_1_CLS = new SomeDocument1Entity(
        id: 1L,
        number: 2L,
        someLocalDate: LocalDate.of(2022, 12, 1)
    )

    public static final MapperGenerateConfiguration FEW_ENUM_MAPPINGS_CONF = withMapperConfigurations(
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
    )

    public static final GenericMapperArgument METAMODEL_WITH_ENUMS1_MDL = emptyGenericMapperArgument(
        enum1: SomeEnum1.UNKNOWN,
        enum2: SomeEnum1.VAL3,
        enum3: "VAL2",
        enum4: "UNKNOWN",
        stringToMetamodelOfEnum: "OTH1",
        metamodelOfEnumToString: "OTH1",
        stringToEnumByNativeSpringConversions: "OTH",
        stringToEnumByCrudWizardServiceConversion: "TEST",
        stringToEnumMetaModelByCrudWizardServiceConversion: "3",
    )

    public static final METAMODEL_WITH_ENUMS2_MDL = [
        enum1                                             : "VAL1",
        enum2                                             : SomeEnum2.OTH2,
        enum3                                             : SomeEnum2.OTH2,
        enum4                                             : "UNKNOWN",
        stringToMetamodelOfEnum                           : "OTH1",
        metamodelOfEnumToString                           : "OTH1",
        stringToEnumByNativeSpringConversions             : SomeEnum1.OTH,
        stringToEnumByCrudWizardServiceConversion         : SomeEnum2.UNKNOWN,
        stringToEnumMetaModelByCrudWizardServiceConversion: "VAR3",
    ]

    public static final MapperGenerateConfiguration ENUM_CONFLICTS_CONF = withMapperConfigurations(MapperConfiguration.builder()
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
    )

    public static final LinkedHashMap<String, SomeEnum3> METAMODEL_WITH_ENUMS4_MDL = [
        spec1enum: SomeEnum3.SPEC1,
        spec2enum: SomeEnum3.SPEC2
    ]

    public static final GenericMapperArgument METAMODEL_WITH_ENUMS3_MDL = emptyGenericMapperArgument([
        spec1enum: "OTH",
        spec2enum: "OTH"
    ])

    public static final MapperGenerateConfiguration MAPPING_COLLECTION_WITH_STAR_CONF = withMapperConfigurations(MapperConfiguration.builder()
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
                                field3: PropertiesOverriddenMapping.builder()
                                    .valueMappingStrategy([
                                        new NullAssignExpression(modelFromClass(Long))
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
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .ignoredFields(["field1"])
                .build())
            .build(),
        MapperConfiguration.builder()
            .name("mapElements2")
            .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
            .targetMetaModel(modelFromClass(CollectionElement))
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .ignoredFields(["field2"])
                .build())
            .build(),
        MapperConfiguration.builder()
            .name("mapFromOtherElement")
            .sourceMetaModel(modelFromClass(CollectionElementOther))
            .targetMetaModel(modelFromClass(CollectionElement))
            .build()
    )

    public static final GenericMapperArgument OTHER_WITH_ELEMENTS_MDL = emptyGenericMapperArgument([
        elements1      : [
            [
                field1    : "field1_1",
                field2    : "field2_1",
                field3    : 1L,
                someObject: [
                    name   : "nameValue1",
                    surname: "surnameValue1",
                ],
            ],
            [
                field1    : "field1_2",
                field2    : "field2_2",
                field3    : 2L,
                someObject: [
                    name   : "nameValue2",
                    surname: "surnameValue2",
                ],
            ]
        ],
        elements2      : [
            [
                field1    : "field1_2_1",
                field2    : "field2_2_1",
                field3    : 3L,
                someObject: [
                    name   : "nameValue1",
                    surname: "surnameValue1",
                ],
            ],
        ] as Set,
        elements3      : [
            [
                field1    : "field1_3_1",
                field2    : "field2_3_1",
                field3    : 4L,
                someObject: [
                    name   : "nameValue3",
                    surname: "surnameValue3",
                ],
            ],
        ] as Set,
        someOneElement : [
            field1    : "field1_one",
            field2    : "field2_one",
            field3    : 11L,
            someObject: [
                name   : "nameValueOne",
                surname: "surnameValueOne",
            ],
        ],
        someOneElement2: new CollectionElementOther(
            field1: "someOneField1_elem2",
            field2: "field_elem2",
            field3: 22L,
            someObject: new NestedElementObject(
                name: "nameValue_elem2",
                surname: "surnameValue_elem2",
            )
        ),
    ])

    public static final OtherWithElements OTHER_WITH_ELEMENTS_CLS = new OtherWithElements(
        elements1: [
            new CollectionElement(
                null,
                "field2_one",
                11L,
                new NestedElementObject(
                    name: "nameValueOne",
                    surname: "surnameValueOne",
                )
            ),
            new CollectionElement(
                null,
                "field2_1",
                1L,
                new NestedElementObject(
                    name: "nameValue1",
                    surname: "surnameValue1",
                )
            ),
            new CollectionElement(
                null,
                "field2_2",
                2L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            new CollectionElement(
                "someOneField1_elem2",
                "field_elem2",
                22L,
                new NestedElementObject(
                    name: "nameValue_elem2",
                    surname: "surnameValue_elem2",
                )
            )
        ],
        elements2: [
            new CollectionElement(
                "field1_2_1",
                null,
                3L,
                new NestedElementObject(
                    name: "nameValue1",
                    surname: "surnameValue1",
                )
            ),
        ],
        elements3: [
            new CollectionElement(
                "field1_3_1",
                "field2_3_1",
                null,
                new NestedElementObject(
                    name: "nameValue3",
                    surname: "surnameValue3",
                )
            )
        ]
    )

    public static final OtherWithElements OTHER_WITH_ELEMENTS_CLS2 = new OtherWithElements(
        elements1: [
            new CollectionElement(
                null,
                "field2_one",
                11L,
                new NestedElementObject(
                    name: "nameValueOne",
                    surname: "surnameValueOne",
                )
            ),
            new CollectionElement(
                "field1_1",
                null,
                1L,
                new NestedElementObject(
                    name: "nameValue1",
                    surname: "surnameValue1",
                )
            ),
            new CollectionElement(
                "field1_2",
                null,
                2L,
                new NestedElementObject(
                    name: "nameValue2",
                    surname: "surnameValue2",
                )
            ),
            new CollectionElement(
                "someOneField1_elem2",
                "field_elem2",
                22L,
                new NestedElementObject(
                    name: "nameValue_elem2",
                    surname: "surnameValue_elem2",
                )
            )
        ],
        elements2: [
            new CollectionElement(
                "field1_2_1",
                null,
                3L,
                new NestedElementObject(
                    name: "nameValue1",
                    surname: "surnameValue1",
                )
            ),
        ],
        elements3: [
            new CollectionElement(
                "field1_3_1",
                "field2_3_1",
                null,
                new NestedElementObject(
                    name: "nameValue3",
                    surname: "surnameValue3",
                )
            )
        ]
    )

    public static final MapperGenerateConfiguration EACH_MAP_BY_CONF = withMapperConfigurations(MapperConfiguration.builder()
        .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName([
                elements1: PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([
                        new EachElementMapByMethodAssignExpression("mapElements1",
                            createFieldsChainExpression(OTHER_WITH_ELEMENTS_MODEL, "someOneElement")),
                        new EachElementMapByMethodAssignExpression("mapElements2",
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
                                field3: PropertiesOverriddenMapping.builder()
                                    .valueMappingStrategy([
                                        new NullAssignExpression(modelFromClass(Long))
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
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .ignoredFields(["field1"])
                .build())
            .build(),
        MapperConfiguration.builder()
            .name("mapElements2")
            .sourceMetaModel(COLLECTION_ELEMENT_MODEL)
            .targetMetaModel(modelFromClass(CollectionElement))
            .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
                .ignoredFields(["field2"])
                .build())
            .build(),
        MapperConfiguration.builder()
            .name("mapFromOtherElement")
            .sourceMetaModel(modelFromClass(CollectionElementOther))
            .targetMetaModel(modelFromClass(CollectionElement))
            .build()
    )

    public static final ClassMetaModel LIST_OF_METAMODELS = ClassMetaModel.builder()
        .realClass(List)
        .genericTypes([COLLECTION_ELEMENT_MODEL])
        .build()

    public static final ClassMetaModel SET_OF_CLASSES = ClassMetaModel.builder()
        .realClass(Set)
        .genericTypes([modelFromClass(CollectionElement)])
        .build()

    public static final GenericMapperArgument LIST_OF_METAMODELS_SAMPLE = emptyGenericMapperArgument([
        [
            field1    : "field1_1",
            field2    : "field2_1",
            field3    : 1L,
            someObject: [
                name   : "nameValue1",
                surname: "surnameValue1",
            ],
        ]
    ])

    public static final LIST_OF_ELEMENTS_CLS = [new CollectionElement(
        "field1_1",
        "field2_1",
        1L,
        new NestedElementObject(
            name: "nameValue1",
            surname: "surnameValue1",
        )
    )] as Set

    public static final ClassMetaModel LIST_LIST_MODEL = ClassMetaModel.builder()
        .realClass(List)
        .genericTypes([ClassMetaModel.builder()
                           .realClass(List)
                           .genericTypes([COLLECTION_ELEMENT_MODEL])
                           .build()])
        .build()

    public static final ClassMetaModel LIST_SET_ELEMENT = ClassMetaModel.builder()
        .realClass(Set)
        .genericTypes([ClassMetaModel.builder()
                           .realClass(List)
                           .genericTypes([modelFromClass(CollectionElement)])
                           .build()])
        .build()

    public static final GenericMapperArgument LIST_LIST_MODEL_SAMPLE = emptyGenericMapperArgument([
        [
            [
                field1    : "field1_1",
                field2    : "field2_1",
                field3    : 1L,
                someObject: [
                    name   : "nameValue1",
                    surname: "surnameValue1",
                ],
            ]
        ]
    ])

    public static final LIST_SET_ELEMENT_SAMPLE = [[new CollectionElement(
        "field1_1",
        "field2_1",
        1L,
        new NestedElementObject(
            name: "nameValue1",
            surname: "surnameValue1",
        )
    )]] as Set

    public static final GenericMapperArgument LIST_AS_LIST_GENERIC_TYPE_1_CLS = emptyGenericMapperArgument(new ListAsListGenericType1(
        [
            [
                new CollectionElement(
                    "field1_1",
                    "field2_1",
                    1L,
                    new NestedElementObject(
                        name: "nameValue1",
                        surname: "surnameValue1",
                    )
                )]]
    ))

    public static final ListAsListGenericType2 LIST_AS_LIST_GENERIC_TYPE_2_CLS = new ListAsListGenericType2(
        [
            [
                new ListAsListGenericType2.NestedCollectionElement(
                    "field1_1",
                    "field2_1",
                    1L,
                    new NestedElementObject(
                        name: "nameValue1",
                        surname: "surnameValue1",
                    )
                )] as ListAsListGenericType2.NestedCollectionElement[]
        ] as Set
    )

    public static final ClassMetaModel DOCUMENT_MODEL = ClassMetaModel.builder()
        .name("document")
        .fields([
            createValidFieldMetaModel("ID", Long),
            createValidFieldMetaModel("serialNumber", String)]
        )
        .build()

    public static final ClassMetaModel PERSON_MODEL = ClassMetaModel.builder()
        .name("person")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("name", String),
            createValidFieldMetaModel("document", DOCUMENT_MODEL)
        ]
        )
        .build()

    public static final def PERSON_MODEL_MDL = emptyGenericMapperArgument([
        id : 1L,
        name: "personName",
        document: [
            ID: 11L,
            serialNumber: "XCD_2304895"
        ]
    ])

    public static final def PERSON_MODEL_2_MDL = [
        id : 1L,
        name: "personName",
        documentTarget: [
            uuid: "1234-1211",
            number: "PL_XCD_2304895"
        ]
    ]

    public static final DOCUMENT_OTHER_MODEL = ClassMetaModel.builder()
        .name("documentOther")
        .fields([
            createValidFieldMetaModel("uuid", String),
            createValidFieldMetaModel("number", String)]
        )
        .build()

    public static final ClassMetaModel PERSON_2_MODEL = ClassMetaModel.builder()
        .name("person2")
        .fields([
            createValidFieldMetaModel("id", Long),
            createValidFieldMetaModel("name", String),
            createValidFieldMetaModel("documentTarget", DOCUMENT_OTHER_MODEL)
        ]
        )
        .build()

    public static final MapperGenerateConfiguration PERSON_MAPPING_CONF = withMapperConfigurations(MapperConfiguration.builder()
        .propertyOverriddenMapping(PropertiesOverriddenMapping.builder()
            .mappingsByPropertyName([
                documentTarget: PropertiesOverriddenMapping.builder()
                    .valueMappingStrategy([
                        new ByMapperNameAssignExpression(
                            DOCUMENT_OTHER_MODEL,
                            new FieldsChainToAssignExpression(PERSON_MODEL,
                                "rootSourceObject", [PERSON_MODEL.getFieldByName("document")]),
                            "personToPerson2Mapper"
                        )
                    ])
                    .build()
            ])
            .build()).build())
}
