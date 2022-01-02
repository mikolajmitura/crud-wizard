package pl.jalokim.crudwizard.genericapp.rest

import static org.springframework.http.HttpMethod.PUT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.core.datastorage.query.RealExpression.isEqualsTo
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntryWithErrorCode
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createHttpQueryParamsClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.isIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DataStorageQuerySamples.createDsQuery
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDtoSamples.createQueryProviderDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidEndpointResponseMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidGetListOfPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators2
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostWithSimplePerson
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDtoSamples.sampleJoinerDto
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDtoSamples.createMapperMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.notNullValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractErrorResponseDto
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractResponseAsClass
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractResponseAsLong
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.entityNotFoundMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidSizeMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.SIZE_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.datastorage.DataStorage
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDto
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.CreatePersonFinalMapper
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.FinalMapperForFewDs
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.GetByIdFromFewDsMapper
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.PersonDocumentInThirdDbIdMapper
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.PersonToSecondDbMapper
import pl.jalokim.crudwizard.genericapp.rest.samples.mapper.PersonToThirdDbMapper
import pl.jalokim.crudwizard.genericapp.rest.samples.query.FinalQueryProvider1
import pl.jalokim.crudwizard.genericapp.rest.samples.query.FinalQueryProviderForPersonResultEntry
import pl.jalokim.crudwizard.genericapp.rest.samples.query.SecondDbPersonGetOneQuery
import pl.jalokim.crudwizard.genericapp.rest.samples.query.ThirdQueryFindByIdFromFirstQueryResult
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints
import pl.jalokim.utils.test.DataFakerHelper

class GenericRestControllerIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private RawOperationsOnEndpoints rawOperationsOnEndpoints

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private DataStorage dataStorage

    /**
     * checking that @Validated was invoked on delegated NormalSpringService.createSamplePersonDtoWithValidated
     * test case with custom service bean
     */
    def "invoked validation in NormalSpringService.createSamplePersonDtoWithValidated on @Validated field and return validation errors"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .className(NormalSpringService.canonicalName)
                .beanName("normalSpringService")
                .methodName("createSamplePersonDtoWithValidated")
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"),
            new ExampleUser(name: DataFakerHelper.randomText()))

        then:
        def errorResponse = extractErrorResponseDto(httpResponse)
        httpResponse.andExpect(status().isBadRequest())
        assertValidationResults(errorResponse.getErrors(), [
            errorEntry("surname", notNullMessage())
        ])
    }

    /**
     * return success value of custom service via NormalSpringService.createSamplePersonDtoWithValidated
     */
    def "return expected value of NormalSpringService.createSamplePersonDtoWithValidated with ok"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .className(NormalSpringService.canonicalName)
                .beanName("normalSpringService")
                .methodName("createSamplePersonDtoWithValidated")
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)
        def name = DataFakerHelper.randomText()
        def surname = DataFakerHelper.randomText()

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"),
            new ExampleUser(name: name, surname: surname))
        def samplePersonDto = extractResponseAsClass(httpResponse, SamplePersonDto)

        then:
        httpResponse.andExpect(status().isCreated())
        samplePersonDto == SamplePersonDto.builder().id(1L).name(name).surname(surname).build()
    }

    def "invoke endpoints with default generic service, default mappers, use one default data storage with success"() {
        given:
        def createPostPersonEndpoint = createValidPostExtendedUserWithValidators2()
        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)
        def personPayload = createPostValidPersonPayload()

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personPayload)

        then:
        httpResponse.andExpect(status().isCreated())
        def createdId = rawOperationsOnEndpoints.extractResponseAsId(httpResponse)
        def personMetaModel = findClassMetaModelByName(createPostPersonEndpoint.payloadMetamodel.name)
        def returnedObject = dataStorage.getEntityById(personMetaModel, createdId)
        returnedObject != null
        returnedObject.id == createdId
        returnedObject.name == personPayload.name
        returnedObject.surname == personPayload.surname

        and: 'get via REST by id'
        def createGetByIdPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("users/{userId}")
            .operationName("getUserById")
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .httpMethod(HttpMethod.GET)
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(ClassMetaModelDto.builder()
                    .id(personMetaModel.id)
                    .name(personMetaModel.name)
                    .build())
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createGetByIdPersonEndpoint)

        when:
        def getByIdResponse = rawOperationsOnEndpoints.getAndReturnJson("/users/$createdId")

        then:
        getByIdResponse.id == createdId
        getByIdResponse.name == personPayload.name
        getByIdResponse.surname == personPayload.surname

        and: 'update via REST by id'
        def createUpdateByIdPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("users/{userId}")
            .operationName("updateUser")
            .payloadMetamodel(ClassMetaModelDto.builder()
                .id(personMetaModel.id)
                .name(personMetaModel.name)
                .build())
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .httpMethod(HttpMethod.PUT)
            .payloadMetamodelAdditionalValidators([
                AdditionalValidatorsMetaModelDto.builder()
                    .fullPropertyPath("id")
                    .validators([notNullValidatorMetaModelDto()])
                    .build()
            ])
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createUpdateByIdPersonEndpoint)
        def earlierSurNameValue = personPayload.surname
        personPayload.setSurname(randomText(15))
        personPayload.setId(createdId)

        when:
        rawOperationsOnEndpoints.putPayload("/users/$createdId", personPayload)

        then:
        def personIdDsAfterUpdate = dataStorage.getEntityById(personMetaModel, createdId)
        personIdDsAfterUpdate != null
        personIdDsAfterUpdate.id == createdId
        personIdDsAfterUpdate.name == personPayload.name
        personIdDsAfterUpdate.surname == personPayload.surname
        personIdDsAfterUpdate.surname != earlierSurNameValue

        and: "delete object by id"
        def createDeleteByIdPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("users/{userId}")
            .operationName("deleteUser")
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .httpMethod(HttpMethod.DELETE)
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(findDataStorageMetaModelDtoByClass(InMemoryDataStorage))
                    .classMetaModelInDataStorage(createClassMetaModelDtoWithId(personMetaModel.id))
                    .build()
            ])
            .build()

        endpointMetaModelService.createNewEndpoint(createDeleteByIdPersonEndpoint)

        when:
        rawOperationsOnEndpoints.delete("/users/$createdId")

        then:
        dataStorage.getOptionalEntityById(personMetaModel, createdId).isEmpty()

        and: 'should not find object via REST by id'
        when:
        def response = rawOperationsOnEndpoints.perform(MockMvcRequestBuilders.get("/users/$createdId"))

        then:
        response.andExpect(status().isNotFound())
        def errorResponse = extractErrorResponseDto(response)
        errorResponse.message == entityNotFoundMessage(createdId, personMetaModel.name)

        and: 'return people list with default query provider'
        def getListOfPersonEndpoint = createValidGetListOfPerson().toBuilder()
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelDtoWithId(personMetaModel.id)])
                    .build())
                .build()
            )
            .build()

        endpointMetaModelService.createNewEndpoint(getListOfPersonEndpoint)

        def payloadPerson1 = createPostValidPersonPayload("John", "Doe")
        def person1Id = rawOperationsOnEndpoints.postAndReturnLong("/users", payloadPerson1)
        def payloadPerson2 = createPostValidPersonPayload("John", "Kowalskydo")
        def person2Id = rawOperationsOnEndpoints.postAndReturnLong("/users", payloadPerson2)
        def payloadPerson3 = createPostValidPersonPayload("Patrick", "Uknown")
        def person3Id = rawOperationsOnEndpoints.postAndReturnLong("/users", payloadPerson3)
        def payloadPerson4 = createPostValidPersonPayload("Johzz", "Modoe")
        def person4Id = rawOperationsOnEndpoints.postAndReturnLong("/users", payloadPerson4)

        when:
        def peopleList1 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person")
        def peopleList2 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person", [name: "John"])
        def peopleList3 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person", [name: "John", surname: "doe"])
        def peopleList4 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person", [surname: "do", "sort_by": "name(Desc),surname(Desc)"])
        def peopleList5 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person", [surname: "do", "sort_by": "name,surname(Desc)"])

        then:
        peopleList1.collect{ it.id } == [person1Id, person2Id, person3Id, person4Id]
        peopleList2.collect{ it.id } == [person1Id, person2Id]
        peopleList3.collect{ it.id } == [person1Id]
        peopleList4.collect{ it.id } == [person4Id, person2Id, person1Id]
        peopleList5.collect{ it.id } == [person2Id, person1Id, person4Id]

        verifyAll(peopleList3[0]) {
            id == person1Id
            name == payloadPerson1.name
            surname == payloadPerson1.surname
        }

        and: 'return people list with default query provider and with final query provider'
        def getListOfPersonEndpointWithFinalQuery = createValidGetListOfPerson().toBuilder()
            .baseUrl("domain/person/final_query")
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .queryProvider(createQueryProviderDto(FinalQueryProvider1))
                .classMetaModel(createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelDtoWithId(personMetaModel.id)])
                    .build())
                .build()
            )
            .build()

        endpointMetaModelService.createNewEndpoint(getListOfPersonEndpointWithFinalQuery)

        when:
        def peopleListWithFinalQuery1 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person/final_query")
        def peopleListWithFinalQuery2 = rawOperationsOnEndpoints.getAndReturnArrayJson("/domain/person/final_query", [surname: "kow"])

        then:
        peopleListWithFinalQuery1.collect{ it.id } == [person1Id, person2Id]
        peopleListWithFinalQuery2.collect{ it.id } == [person2Id]

        // TODO #37 test for return page
    }

    def "invoke endpoints with default generic service, with mappers in java, use 3 data storages with success"() {
        given: 'POST to few data storages'
        long defaultDataStorageId = metaModelContextService.getMetaModelContext().getDefaultDataStorageMetaModel().getId()
        def defaultDataStorageDto = createDataStorageMetaModelDtoWithId(defaultDataStorageId)

        def personInSecondDbModel = ClassMetaModelDto.builder()
            .name("personSecondDb")
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("uuid", String, [], [isIdFieldType()]),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("firstDbId", Long) // person.id
            ])
            .build()

        def personDocumentInThirdDbModel = ClassMetaModelDto.builder()
            .name("personDocumentDb")
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("uuid", String, [], [isIdFieldType()]),
                createValidFieldMetaModelDto("firstDbId", Long), // person.id
                createValidFieldMetaModelDto("documentValue", String) // person.document.value
            ])
            .build()

        def secondDbName = "second-db"
        def thirdDbName = "third-db"
        def createPostPersonEndpoint = createValidPostExtendedUserWithValidators2().toBuilder()
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(null, defaultDataStorageDto),
                createSampleDataStorageConnectorDto(personInSecondDbModel,
                    createDataStorageMetaModelDto(secondDbName),
                    createMapperMetaModelDto(PersonToSecondDbMapper.class, "personToSecondDbMapperCreate")),
                createSampleDataStorageConnectorDto(personDocumentInThirdDbModel,
                    createDataStorageMetaModelDto(thirdDbName),
                    createMapperMetaModelDto(PersonToThirdDbMapper.class, "personToThirdDbMapperCreate"))
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(Long))
                .mapperMetaModel(createMapperMetaModelDto(CreatePersonFinalMapper.class, "returnIdFromDefaultDs"))
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)
        def personCreatePayload = createPostValidPersonPayload()

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personCreatePayload)

        then:
        httpResponse.andExpect(status().isCreated())
        def personSavedIds = rawOperationsOnEndpoints.extractResponseAsJson(httpResponse)
        long savedPersonId = personSavedIds.personId
        def personDocumentUUID = personSavedIds.personDocumentUUID

        def metaContext = metaModelContextService.getMetaModelContext()
        def dataStorages = metaContext.getDataStorages().fetchAll()
        dataStorages.size() == 3
        def defaultDataStorage = metaModelContextService.getDataStorageByName(InMemoryDataStorage.DEFAULT_DS_NAME)
        def secondDataStorage = metaModelContextService.getDataStorageByName(secondDbName)
        def thirdDataStorage = metaModelContextService.getDataStorageByName(thirdDbName)

        def personClassModel = metaModelContextService.getClassMetaModelByName("person")
        def personSecondDbClassModel = metaModelContextService.getClassMetaModelByName("personSecondDb")
        def personDocumentDbClassModel = metaModelContextService.getClassMetaModelByName("personDocumentDb")

        def personEntryNumberIn1Db = defaultDataStorage.count(createDsQuery(personClassModel))
        def personEntryNumberIn2Db = secondDataStorage.count(createDsQuery(personSecondDbClassModel))
        def personEntryNumberIn3Db = thirdDataStorage.count(createDsQuery(personDocumentDbClassModel))
        personEntryNumberIn1Db == 1
        personEntryNumberIn2Db == 1
        personEntryNumberIn3Db == 1
        def uuidForPersonIn2Db = secondDataStorage.findEntities(createDsQuery(personSecondDbClassModel)).find { it }.uuid

        and: 'get via REST by id'
        def createGetByIdPersonEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("users/{userId}")
            .operationName("getUserById")
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .httpMethod(HttpMethod.GET)
            .queryArguments(
                ClassMetaModelDto.builder()
                    .name("queryArguments")
                    .fields([createValidFieldMetaModelDto("thirdDbId", String)])
                    .build())
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .mapperMetaModel(createMapperMetaModelDto(GetByIdFromFewDsMapper, "mapPersonsResultsToOne"))
                .classMetaModel(ClassMetaModelDto.builder()
                    .name("personFrom3Dbs")
                    .fields([
                        createValidFieldMetaModelDto("firstDb", createClassMetaModelDtoWithId(personClassModel.id)),
                        createValidFieldMetaModelDto("secondDb", createClassMetaModelDtoWithId(personSecondDbClassModel.id)),
                        createValidFieldMetaModelDto("thirdDb", createClassMetaModelDtoWithId(personDocumentDbClassModel.id))
                    ])
                    .build())
                .build())
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personClassModel.id), defaultDataStorageDto),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personSecondDbClassModel.id),
                    createDataStorageMetaModelDto(secondDbName),
                    null, null,
                    createQueryProviderDto(SecondDbPersonGetOneQuery)
                ),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personDocumentDbClassModel.id),
                    createDataStorageMetaModelDto(thirdDbName),
                    null,
                    createMapperMetaModelDto(PersonDocumentInThirdDbIdMapper, "mapToUuid"))
            ])
            .build()
        endpointMetaModelService.createNewEndpoint(createGetByIdPersonEndpoint)

        when:
        def getByIdResponse = rawOperationsOnEndpoints.getAndReturnJson("/users/$savedPersonId", [thirdDbId: personDocumentUUID])

        then:
        verifyAll(getByIdResponse) {
            verifyAll(firstDb) {
                id == savedPersonId
                name == personCreatePayload.name
                surname == personCreatePayload.surname
                birthDate == null
                verifyAll(document) {
                    type == personCreatePayload.document.type
                    value == personCreatePayload.document.value
                    enumField == "ENUM1"
                }
                verifyAll(documents[0]) {
                    type == personCreatePayload.document.type
                    value == personCreatePayload.document.value
                    enumField == "ENUM1"
                }
            }
            verifyAll(secondDb) {
                uuid == uuidForPersonIn2Db
                name == personCreatePayload.name
                surname == personCreatePayload.surname
                firstDbId == savedPersonId
            }
            verifyAll(thirdDb) {
                uuid == personDocumentUUID
                firstDbId == savedPersonId
                documentValue == personCreatePayload.document.value
            }
        }

        and: 'update in few DS'
        def createPutPersonEndpoint = EndpointMetaModelDto.builder()
            .httpMethod(PUT)
            .operationName("updatePerson")
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .baseUrl("users/{userId}")
            .payloadMetamodel(createClassMetaModelDtoWithId(personClassModel.id))
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personClassModel.id), defaultDataStorageDto),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personSecondDbClassModel.id),
                    createDataStorageMetaModelDto(secondDbName),
                    createMapperMetaModelDto(PersonToSecondDbMapper.class, "personToSecondDbMapperCreate")),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personDocumentDbClassModel.id),
                    createDataStorageMetaModelDto(thirdDbName),
                    createMapperMetaModelDto(PersonToThirdDbMapper.class, "personToThirdDbMapperCreate"))
            ])
            .queryArguments(
                ClassMetaModelDto.builder()
                    .name("queryArguments")
                    .fields([createValidFieldMetaModelDto("thirdDbId", String)])
                    .build())
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createPutPersonEndpoint)

        def personUpdatePayload = createPutValidPersonPayload(personCreatePayload, savedPersonId)

        when:
        rawOperationsOnEndpoints.putPayload("/users/$savedPersonId", personUpdatePayload, [thirdDbId: personDocumentUUID])

        then:
        def personIdFirstDb = defaultDataStorage.getEntityById(personClassModel, savedPersonId)
        def personIdSecondDb = secondDataStorage.getEntityById(personSecondDbClassModel, uuidForPersonIn2Db)
        def documentInThirdDb = thirdDataStorage.getEntityById(personDocumentDbClassModel, personDocumentUUID)

        verifyAll(personIdFirstDb) {
            name == personCreatePayload.name
            surname == personUpdatePayload.surname
            document.value == personUpdatePayload.document.value
        }

        verifyAll(personIdSecondDb) {
            name == personCreatePayload.name
            surname == personUpdatePayload.surname
        }

        verifyAll(documentInThirdDb) {
            documentValue == personUpdatePayload.document.value
        }

        def personEntryNumberIn1DbAfterUpdate = defaultDataStorage.count(createDsQuery(personClassModel))
        def personEntryNumberIn2DbAfterUpdate = secondDataStorage.count(createDsQuery(personSecondDbClassModel))
        def personEntryNumberIn3DbAfterUpdate = thirdDataStorage.count(createDsQuery(personDocumentDbClassModel))
        personEntryNumberIn1DbAfterUpdate == 1
        personEntryNumberIn2DbAfterUpdate == 1
        personEntryNumberIn3DbAfterUpdate == 1

        and: 'delete via REST by id'
        def createDeleteByIdPersonEndpoint = EndpointMetaModelDto.builder()
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .baseUrl("users/{userId}")
            .httpMethod(HttpMethod.DELETE)
            .operationName("deletePersonById")
            .queryArguments(
                ClassMetaModelDto.builder()
                    .name("queryArguments")
                    .fields([createValidFieldMetaModelDto("thirdDbId", String)])
                    .build())
            .pathParams(ClassMetaModelDto.builder()
                .name("pathParams")
                .fields([createValidFieldMetaModelDto("userId", Long)])
                .build())
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personClassModel.id), defaultDataStorageDto),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personSecondDbClassModel.id),
                    createDataStorageMetaModelDto(secondDbName),
                    null, null,
                    createQueryProviderDto(SecondDbPersonGetOneQuery)
                ),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personDocumentDbClassModel.id),
                    createDataStorageMetaModelDto(thirdDbName),
                    null,
                    createMapperMetaModelDto(PersonDocumentInThirdDbIdMapper, "mapToUuid"))
            ])
            .build()

        endpointMetaModelService.createNewEndpoint(createDeleteByIdPersonEndpoint)

        when:
        rawOperationsOnEndpoints.delete("/users/$savedPersonId", [thirdDbId: personDocumentUUID])

        then:
        def personEntryNumberIn1DbAfterDelete = defaultDataStorage.count(createDsQuery(personClassModel))
        def personEntryNumberIn2DbAfterDelete = secondDataStorage.count(createDsQuery(personSecondDbClassModel))
        def personEntryNumberIn3DbAfterDelete = thirdDataStorage.count(createDsQuery(personDocumentDbClassModel))
        personEntryNumberIn1DbAfterDelete == 0
        personEntryNumberIn2DbAfterDelete == 0
        personEntryNumberIn3DbAfterDelete == 0
        def httpGetOneResponse = rawOperationsOnEndpoints.performQuery("/users/$savedPersonId", [thirdDbId: personDocumentUUID])
        httpGetOneResponse.andExpect(status().isNotFound())

        and: 'get list without final query'

        def personPayload1 = createPostValidPersonPayload("John", "Doe")
        def personPayload2 = createPostValidPersonPayload("John", "Hodoes")
        def personPayload3 = createPostValidPersonPayload("Adam", "Adams")

        rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personPayload1)
        rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personPayload2)
        rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personPayload3)

        def nameOnlyIn2Db = "John"
        def surnameOnlyIn2Db = "Novdoe"
        secondDataStorage.saveOrUpdate(personSecondDbClassModel, [name: nameOnlyIn2Db, surname: surnameOnlyIn2Db])

        def createGetListWithoutFinalQueryPersonEndpoint = EndpointMetaModelDto.builder()
            .apiTag(createApiTagDtoByName(createPostPersonEndpoint.apiTag.name))
            .baseUrl("users")
            .httpMethod(HttpMethod.GET)
            .operationName("getNormalListOfPeople")
            .queryArguments(
                ClassMetaModelDto.builder()
                    .name("queryArguments")
                    .fields([
                        createValidFieldMetaModelDto("name", String),
                        createValidFieldMetaModelDto("surname", String),
                    ])
                    .build())
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personClassModel.id), defaultDataStorageDto),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personSecondDbClassModel.id),
                    createDataStorageMetaModelDto(secondDbName), null, null, null, "second-query"
                ),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoWithId(personDocumentDbClassModel.id),
                    createDataStorageMetaModelDto(thirdDbName),
                    null,
                    null, createQueryProviderDto(ThirdQueryFindByIdFromFirstQueryResult))
            ])
            .dataStorageResultsJoiners([
                sampleJoinerDto(InMemoryDataStorage.DEFAULT_DS_NAME, "id", "second-query", "firstDbId"),
                sampleJoinerDto(InMemoryDataStorage.DEFAULT_DS_NAME, "id", "third-db", "firstDbId"),
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .mapperMetaModel(createMapperMetaModelDto(FinalMapperForFewDs, "mapResults"))
                .classMetaModel(createListWithMetaModel(ClassMetaModelDto.builder()
                        .name("personResultEntry")
                        .fields([
                            createValidFieldMetaModelDto("uuid2", String),
                            createValidFieldMetaModelDto("uuid3", String),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("lastname", String),
                            createValidFieldMetaModelDto("documentValue", String),
                            createValidFieldMetaModelDto("documentType", Byte),
                            createValidFieldMetaModelDto("personId", Long),
                        ])
                        .build()
                ))
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createGetListWithoutFinalQueryPersonEndpoint)

        when:
        def getResultOfFewDsResult1 = rawOperationsOnEndpoints.getAndReturnArrayJson("/users")
        def getResultOfFewDsResult2 = rawOperationsOnEndpoints.getAndReturnArrayJson("/users", [surname: "doe"])

        then:
        getResultOfFewDsResult1.size() == 4
        def expectedInputPayloadsForResult1 = [personPayload1, personPayload2, personPayload3]

        assertPayloadExistenceInResponseList(expectedInputPayloadsForResult1, getResultOfFewDsResult1,
            defaultDataStorage, personClassModel, secondDataStorage,
            personSecondDbClassModel, thirdDataStorage, personDocumentDbClassModel)

        assertExistenceJonNovDoe(getResultOfFewDsResult1, nameOnlyIn2Db, surnameOnlyIn2Db,
            secondDataStorage, personSecondDbClassModel)

        getResultOfFewDsResult2.size() == 3
        def expectedInputPayloadsForResult2 = [personPayload1, personPayload2]

        assertPayloadExistenceInResponseList(expectedInputPayloadsForResult2, getResultOfFewDsResult2,
            defaultDataStorage, personClassModel, secondDataStorage,
            personSecondDbClassModel, thirdDataStorage, personDocumentDbClassModel)

        assertExistenceJonNovDoe(getResultOfFewDsResult2, nameOnlyIn2Db, surnameOnlyIn2Db,
            secondDataStorage, personSecondDbClassModel)

        and: 'get list with final query'
        def personResultEntriesListId = metaModelContextService.getMetaModelContext().getClassMetaModels()
            .findOneBy { it.className = List.canonicalName &&
                it.genericTypes.size() == 1 && it.genericTypes[0].name == "personResultEntry" }
        .getId()

        def createGetListWithFinalQueryPersonEndpoint = createGetListWithoutFinalQueryPersonEndpoint.toBuilder()
            .baseUrl("users/with-final-query")
            .responseMetaModel(
                EndpointResponseMetaModelDto.builder()
                    .mapperMetaModel(createMapperMetaModelDto(FinalMapperForFewDs, "mapResults"))
                    .classMetaModel(createClassMetaModelDtoWithId(personResultEntriesListId))
                    .queryProvider(createQueryProviderDto(FinalQueryProviderForPersonResultEntry))
                .build()
            )
            .build()

        endpointMetaModelService.createNewEndpoint(createGetListWithFinalQueryPersonEndpoint)

        when:
        def getResultOfFewDsResultWithFinalQuery = rawOperationsOnEndpoints.getAndReturnArrayJson("/users/with-final-query", [name: "John"])

        then:
        getResultOfFewDsResultWithFinalQuery.size() == 2
        def expectedInputPayloadsForResultWithFinalQuery = [personPayload1, personPayload2]

        assertPayloadExistenceInResponseList(expectedInputPayloadsForResultWithFinalQuery, getResultOfFewDsResultWithFinalQuery,
            defaultDataStorage, personClassModel, secondDataStorage,
            personSecondDbClassModel, thirdDataStorage, personDocumentDbClassModel)

        getResultOfFewDsResultWithFinalQuery.find {
            it.name == nameOnlyIn2Db && it.lastname == surnameOnlyIn2Db
        } == null

        // TODO #37 test for return page, few ds, everywhere other models, mapper in java
        // TODO #37 test for return page, few ds, everywhere other models, mapper in java, with final query
    }

    private boolean assertExistenceJonNovDoe(List<Map> getResultOfFewDsResult1, nameOnlyIn2Db, surnameOnlyIn2Db,
        secondDataStorage, personSecondDbClassModel) {
        def foundEntry = getResultOfFewDsResult1.find {
            it.name == nameOnlyIn2Db && it.lastname == surnameOnlyIn2Db
        }

        verifyAll(foundEntry) {
            uuid2 == secondDataStorage.findEntities(createDsQuery(personSecondDbClassModel,
                isEqualsTo("name", nameOnlyIn2Db).and(isEqualsTo("surname", surnameOnlyIn2Db))))[0].uuid
            uuid3 == null
            name == "John"
            lastname == "Novdoe"
            documentValue == null
            documentType == null
            personId == null
        }
        return true
    }

    private assertPayloadExistenceInResponseList(ArrayList<ExtendedPerson> inputPersonPayload, getResultOfFewDsResult1, defaultDataStorage, personClassModel,
        secondDataStorage, personSecondDbClassModel, thirdDataStorage, personDocumentDbClassModel) {
        inputPersonPayload.forEach({inputPayload ->
            def foundEntry = getResultOfFewDsResult1.find {
                it.name == inputPayload.name && it.lastname == inputPayload.surname
            }
            assert foundEntry != null
            def whereExpression = isEqualsTo("name", inputPayload.name).and(isEqualsTo("surname", inputPayload.surname))
            long personIdInFirstDb = defaultDataStorage.findEntities(createDsQuery(personClassModel, whereExpression))[0].id

            verifyAll(foundEntry) {
                uuid2 == secondDataStorage.findEntities(createDsQuery(personSecondDbClassModel,
                    isEqualsTo("firstDbId", personIdInFirstDb)))[0].uuid
                uuid3 == thirdDataStorage.findEntities(createDsQuery(personDocumentDbClassModel,
                    isEqualsTo("firstDbId", personIdInFirstDb)))[0].uuid
                name == inputPayload.name
                lastname == inputPayload.surname
                documentValue == inputPayload.document.value
                documentType == inputPayload.document.type
                personId == personIdInFirstDb
            }
        })
        return true
    }

    def "cannot map custom enum value by enum metamodel"() {
        given:
        def createEndpointMetaModelDto = createValidPostExtendedUserWithValidators()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)
        def personInput = createPostValidPersonPayload()
        personInput.documents = [new Document(enumField: "invalid enum")]

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), personInput)

        then:
        httpResponse.andExpect(status().isBadRequest())
        def errorResponse = extractErrorResponseDto(httpResponse)
        errorResponse.message == "invalid enum value : 'invalid enum' in path: documents[0].enumField available enum values: ENUM1, ENUM2"
    }

    def "invoke endpoint with default generic service, mappers, use default data storage with failure"() {
        given:
        def createEndpointMetaModelDto = createValidPostExtendedUserWithValidators2()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), createPostInvalidPersonPayload())

        then:
        httpResponse.andExpect(status().isBadRequest())
        def errorResponse = extractErrorResponseDto(httpResponse)
        assertValidationResults(errorResponse.getErrors(), [
            errorEntry("surname", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("name", invalidSizeMessage(2, 20), SIZE_MESSAGE_PROPERTY),
            errorEntry("documents", invalidSizeMessage(1, null), SIZE_MESSAGE_PROPERTY)
        ])
    }

    def "invoked validation inside of NormalSpringService.createSamplePersonDtoWithValidated by used validation session and return validation errors"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .className(NormalSpringService.canonicalName)
                .beanName("normalSpringService")
                .methodName("validationContextAsArg")
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"),
            new ExampleUser(name: DataFakerHelper.randomText()))

        then:
        httpResponse.andExpect(status().isBadRequest())
        def errorResponse = extractErrorResponseDto(httpResponse)
        assertValidationResults(errorResponse.getErrors(), [
            errorEntryWithErrorCode("_id_surname", "NormalSpringService.invalid.id")
        ])
    }

    def "invoked validation inside of NormalSpringService.createSamplePersonDtoWithValidated by used validation session and without errors"() {
        given:
        def createEndpointMetaModelDto = createValidPostWithSimplePerson().toBuilder()
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .className(NormalSpringService.canonicalName)
                .beanName("normalSpringService")
                .methodName("validationContextAsArg")
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"),
            new ExampleUser(id: DataFakerHelper.randomLong()))

        then:
        httpResponse.andExpect(status().isCreated())
        def createdId = extractResponseAsLong(httpResponse)
        createdId == 10L
    }

    def "return expected translated http query values"() {
        given:
        def someGetEndpoint = EndpointMetaModelDto.builder()
            .baseUrl("some-url")
            .apiTag(ApiTagDto.builder()
                .name(randomText())
                .build())
            .httpMethod(HttpMethod.GET)
            .operationName(randomText())
            .queryArguments(createHttpQueryParamsClassMetaModelDto())
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .className(NormalSpringService.canonicalName)
                .beanName("normalSpringService")
                .methodName("returnTranslatedHttpQuery")
                .build())
            .responseMetaModel(createValidEndpointResponseMetaModelDto().toBuilder()
                .successHttpCode(200)
                .build())
            .build()
        endpointMetaModelService.createNewEndpoint(someGetEndpoint)

        when:
        def response = rawOperationsOnEndpoints.getAndReturnJson("/some-url", [
            some_numbers: "10, 12, 13", some_texts: "text1, text2",
            surname     : "surName", "age": 18
        ])

        then:
        response == [
            some_numbers: [10, 12, 13], some_texts: ["text1", "text2"],
            surname     : "surName", "age": 18
        ]
    }

    private static class ExampleUser {

        Long id
        String name
        String surname
    }

    private static ExtendedPerson createPostValidPersonPayload(String name = randomText(2),
        String surname = randomText(30),
        Document document = new Document(type: 1, value: randomText(5), enumField: "ENUM1")) {

        new ExtendedPerson(name: name, surname: surname, documents: [document], document: document)
    }

    private static ExtendedPerson createPutValidPersonPayload(ExtendedPerson currentState, Long personId) {
        def newSurname = randomText(5)
        def newDocumentValue = randomText(12)
        Document currentDocument = currentState.document
        Document document = new Document(type: currentDocument.type,
            value: newDocumentValue, enumField: currentDocument.enumField)
        new ExtendedPerson(id: personId, name: currentState.name, surname: newSurname, documents: [document], document: document)
    }

    private static ExtendedPerson createPostInvalidPersonPayload() {
        new ExtendedPerson(
            name: randomText(22), documents: []
        )
    }

    private static class ExtendedPerson {

        Long id
        String name
        String surname
        LocalDate birthDate
        List<Document> documents
        Document document
    }

    private static class Document {

        Long id
        Byte type
        String value
        LocalDate validFrom
        LocalDate validTo
        String enumField
    }

    ClassMetaModel findClassMetaModelByName(String name) {
        metaModelContextService.getMetaModelContext().getClassMetaModels()
            .fetchAll().find {
            it.name == name
        }
    }

    ApiTagDto createApiTagDtoByName(String tagName) {
        def tagMetaModel = metaModelContextService.getMetaModelContext().getApiTags()
            .fetchAll().find {
            it.name == tagName
        }
        ApiTagDto.builder()
            .id(tagMetaModel.id)
            .build()
    }

    DataStorageMetaModelDto findDataStorageMetaModelDtoByClass(Class<?> dataStorageClass) {
        def foundDsMetaModel = metaModelContextService.getMetaModelContext().getDataStorages()
            .fetchAll().find {
            it.dataStorage.class == dataStorageClass
        }
        DataStorageMetaModelDto.builder()
            .id(foundDsMetaModel.id)
            .build()
    }
}
