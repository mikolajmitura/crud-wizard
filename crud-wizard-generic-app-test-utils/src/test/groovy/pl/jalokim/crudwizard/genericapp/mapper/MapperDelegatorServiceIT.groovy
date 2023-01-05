package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage.DEFAULT_DS_NAME
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDtoSamples.sampleJoinerDto
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDtoSamples.createMapperMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto.mappingEntry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.mapper.instance.MapperWithNotGenericArguments
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.OtherPersonEntity
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonCreateEvent
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonEntity
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonOneDto
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonTypeEnum
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonTypeEnum2
import pl.jalokim.crudwizard.genericapp.mapper.instance.query.FindByDbIdQueryProvider
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.EnumEntriesMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints

class MapperDelegatorServiceIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private RawOperationsOnEndpoints rawOperationsOnEndpoints

    def "invoking of mappers with other types than GenericMapperArgument"() {
        given:
        def personDtoModel = ClassMetaModelDto.builder()
            .name("personDto")
            .fields([
                createIdFieldType("id", String),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("otherId", Long)
            ])
            .build()

        def entitiesDsDto = createDataStorageMetaModelDto("entities")
        def eventsDsDto = createDataStorageMetaModelDto("events")

        def createPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("super-users/{parentId}/users")
            .operationName("createUserUnderSuperUser")
            .apiTag(ApiTagDto.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.POST)
            .pathParams(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("parentId", Long)
                ])
                .build())
            .payloadMetamodel(personDtoModel)
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(entitiesDsDto)
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonEntity))
                    .mapperMetaModelForPersist(createMapperMetaModelDto(MapperWithNotGenericArguments, "mapToPersonEntity"))
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(eventsDsDto)
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonCreateEvent))
                    .mapperMetaModelForPersist(createMapperMetaModelDto(MapperWithNotGenericArguments, "mapPersonEvent"))
                    .build()
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(Long))
                .mapperMetaModel(createMapperMetaModelDto(MapperWithNotGenericArguments, "getFinalCreateId"))
                .successHttpCode(201)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createPersonEndpoint)

        def createPerson = [
            name   : "Mike",
            surname: "Doe",
            otherId: 345
        ]

        def entitiesDs = metaModelContextService.getDataStorageByName("entities")
        InMemoryDataStorage eventsDs = (InMemoryDataStorage) metaModelContextService.getDataStorageByName("events")
        def entitiesId = metaModelContextService.getDataStorageMetaModelByName("entities").id
        def eventsId = metaModelContextService.getDataStorageMetaModelByName("events").id

        when:
        def createdPersonEntityId = rawOperationsOnEndpoints.postAndReturnLong("/super-users/4/users", createPerson, ["x-logged-user": "admin1"])

        then:
        def foundPersonEntity = entitiesDs.getEntityById(ClassMetaModelFactory.fromRawClass(PersonEntity), createdPersonEntityId)
        verifyAll(foundPersonEntity) {
            id == createdPersonEntityId
            name == createPerson.name
            surname == createPerson.surname
            externalId == createPerson.otherId
            parentId == 4L
            updatedBy == "admin1"
        }

        def storage = eventsDs.getEntitiesByName().get(PersonCreateEvent.canonicalName)
        def personEvents = storage.getEntitiesById().values()
        personEvents.size() == 1
        PersonCreateEvent personEvent = (PersonCreateEvent) personEvents[0]
        verifyAll(personEvent) {
            id != null
            dbId == createdPersonEntityId
            fullName == "${createPerson.name} ${createPerson.surname}"
        }

        and:
        def getPersonEndpointById = EndpointMetaModelDto.builder()
            .baseUrl("super-users/{parentId}/users/{userId}")
            .operationName("getUserUnderSuperUser")
            .apiTag(createApiTagDtoByName("users"))
            .httpMethod(HttpMethod.GET)
            .pathParams(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("parentId", Long),
                    createValidFieldMetaModelDto("userId", Long)
                ])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(entitiesId))
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonEntity))
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(eventsId))
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonCreateEvent))
                    .queryProvider(QueryProviderDto.builder()
                        .className(FindByDbIdQueryProvider.canonicalName)
                        .build())
                    .build()
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(PersonOneDto))
                .mapperMetaModel(createMapperMetaModelDto(MapperWithNotGenericArguments, "getFinalPersonOneDto"))
                .successHttpCode(200)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(getPersonEndpointById)

        when:
        def personOneDto = rawOperationsOnEndpoints.getAndReturnObject("/super-users/4/users/$createdPersonEntityId", PersonOneDto)

        then:
        verifyAll(personOneDto) {
            dbId == createdPersonEntityId
            name == createPerson.name
            surname == createPerson.surname
            externalId == createPerson.otherId
            parentId == 4L
            updatedBy == "admin1"
            eventUuid == personEvent.id
        }

        and:
        def getPersonListEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("super-users/{parentId}/users")
            .operationName("getAllUsersUnderSuperUser")
            .apiTag(createApiTagDtoByName("users"))
            .httpMethod(HttpMethod.GET)
            .pathParams(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("parentId", Long),
                ])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(entitiesId))
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonEntity))
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(eventsId))
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonCreateEvent))
                    .build()
            ])
            .dataStorageResultsJoiners([
                sampleJoinerDto("entities", "id", "events", "dbId")
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createListWithMetaModel(createClassMetaModelDtoFromClass(PersonOneDto)))
                .mapperMetaModel(createMapperMetaModelDto(MapperWithNotGenericArguments, "mapFinalPersonOneDtoByJoinedResultsRow"))
                .successHttpCode(200)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(getPersonListEndpoint)

        when:
        def joinedPersonList = rawOperationsOnEndpoints.getAndReturnCollectionOfObjects("/super-users/4/users", PersonOneDto)

        then:
        joinedPersonList.size() == 1
        verifyAll(joinedPersonList[0]) {
            dbId == createdPersonEntityId
            name == createPerson.name
            surname == createPerson.surname
            externalId == createPerson.otherId
            parentId == 4L
            updatedBy == "****"
            eventUuid == personEvent.id
        }
    }

    def "invoke generated mappers as expected"() {
        def personDtoModel = ClassMetaModelDto.builder()
            .name("personDto")
            .fields([
                createIdFieldType("id", String),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("otherId", Long),
                createValidFieldMetaModelDto("personType", PersonTypeEnum2)
            ])
            .build()

        def createPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("super-users/{parentId}/users")
            .operationName("createUserUnderSuperUser")
            .apiTag(ApiTagDto.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.POST)
            .pathParams(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("parentId", Long)
                ])
                .build())
            .payloadMetamodel(personDtoModel)
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(OtherPersonEntity))
                    .mapperMetaModelForPersist(MapperMetaModelDto.builder()
                        .mapperType(MapperType.GENERATED)
                        .mapperName("dtoToEntityMapper")
                        .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                            .rootConfiguration(MapperConfigurationDto.builder()
                                .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                .targetMetaModel(createClassMetaModelDtoFromClass(OtherPersonEntity))
                                .name("dtoToEntityMapper")
                                .propertyOverriddenMapping([
                                    mappingEntry("externalId", "otherId"),
                                    mappingEntry("parentId", '$pathVariables.parentId'),
                                    mappingEntry("updatedBy", '$headers[\'x-logged-user\']'),
                                    mappingEntry("fromSpringBean", '@serviceForMapper.concatTexts($headers[\'x-logged-user\'], surname)'),
                                ])
                                .build())
                            .subMappersAsMethods([
                                MapperConfigurationDto.builder()
                                    .name("enumMap")
                                    .sourceMetaModel(createClassMetaModelDtoFromClass(PersonTypeEnum2))
                                    .targetMetaModel(createClassMetaModelDtoFromClass(PersonTypeEnum))
                                    .enumEntriesMapping(EnumEntriesMappingDto.builder()
                                        .targetEnumBySourceEnum([
                                            "SMALL"  : "SIMPLE",
                                            "GENERIC": "RAW"
                                        ])
                                        .build())
                                    .build()
                            ])
                            .build())
                        .build())
                    .build()
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(Long))
                .successHttpCode(201)
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createPersonEndpoint)

        def createPerson = [
            name      : "Mike",
            surname   : "Doe",
            otherId   : 345,
            personType: PersonTypeEnum2.SMALL
        ]

        when:
        def createdPersonEntityId = rawOperationsOnEndpoints.postAndReturnLong("/super-users/4/users", createPerson, ["x-logged-user": "admin22"])

        then:
        def entitiesDs = metaModelContextService.getDataStorageByName(DEFAULT_DS_NAME)
        OtherPersonEntity foundPersonEntity = (OtherPersonEntity) entitiesDs
            .getEntityById(ClassMetaModelFactory.fromRawClass(OtherPersonEntity), createdPersonEntityId)
        verifyAll(foundPersonEntity) {
            id == createdPersonEntityId
            name == createPerson.name
            surname == createPerson.surname
            externalId == createPerson.otherId
            parentId == 4L
            updatedBy == "admin22"
            fromSpringBean == "admin22 ${createPerson.surname}"
            personType == PersonTypeEnum.SIMPLE
        }
    }
}
