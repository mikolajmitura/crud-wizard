package pl.jalokim.crudwizard.genericapp.rest

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntryWithErrorCode
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createHttpQueryParamsClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidEndpointResponseMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidGetListOfPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators2
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostWithSimplePerson
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
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDto
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
        def peopleList1 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person")
        def peopleList2 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person", [name: "John"])
        def peopleList3 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person", [name: "John", surname: "doe"])
        def peopleList4 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person", [surname: "do", "sort_by": "name(Desc),surname(Desc)"])
        def peopleList5 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person", [surname: "do", "sort_by": "name,surname(Desc)"])

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
                .queryProvider(QueryProviderDto.builder()
                    .className("pl.jalokim.crudwizard.genericapp.rest.FinalQueryProvider1")
                    .build())
                .classMetaModel(createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelDtoWithId(personMetaModel.id)])
                    .build())
                .build()
            )
            .build()

        endpointMetaModelService.createNewEndpoint(getListOfPersonEndpointWithFinalQuery)

        when:
        def peopleListWithFinalQuery1 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person/final_query")
        def peopleListWithFinalQuery2 = rawOperationsOnEndpoints.getAndReturnArrayJson("domain/person/final_query", [surname: "kow"])

        then:
        peopleListWithFinalQuery1.collect{ it.id } == [person1Id, person2Id]
        peopleListWithFinalQuery2.collect{ it.id } == [person2Id]

        // TODO #37 test for return page
    }

    // TODO #37 test for return get by id, few ds, everywhere other models, mappers in java
    // TODO #37 test for return post, few ds, everywhere other models, mappers in java
    // TODO #37 test for return delete, few ds, everywhere other models, mappers in java
    // TODO #37 test for return list, few ds, everywhere other models, mapper in java
    // TODO #37 test for return list, few ds, everywhere other models, mapper in java, with final query
    // TODO #37 test for return page, few ds, everywhere other models, mapper in java
    // TODO #37 test for return page, few ds, everywhere other models, mapper in java, with final query

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
