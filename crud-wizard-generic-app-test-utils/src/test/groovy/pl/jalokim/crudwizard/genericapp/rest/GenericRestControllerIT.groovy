package pl.jalokim.crudwizard.genericapp.rest

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractErrorResponseDto
import static pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints.extractResponseAsClass
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidSizeMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.SIZE_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.test.utils.RawOperationsOnEndpoints
import pl.jalokim.utils.test.DataFakerHelper

class GenericRestControllerIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private RawOperationsOnEndpoints rawOperationsOnEndpoints

    /**
     * checking that @Validated was invoked on delegated NormalSpringService.createSamplePersonDtoWithValidated
     * test case with custom service bean
     */
    def "invoked validation in NormalSpringService.createSamplePersonDtoWithValidated on @Validated field and return validation errors"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto()
            .toBuilder()
            .payloadMetamodel(simplePersonClassMetaModel())
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
        httpResponse.andExpect(status().isBadRequest())
        def errorResponse = extractErrorResponseDto(httpResponse)

        then:
        assertValidationResults(errorResponse.getErrors(), [
            errorEntry("surname", notNullMessage())
        ])
    }

    /**
     * return success value of custom service via NormalSpringService.createSamplePersonDtoWithValidated
     */
    def "return expected value of NormalSpringService.createSamplePersonDtoWithValidated with ok"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto()
            .toBuilder()
            .payloadMetamodel(simplePersonClassMetaModel())
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
        httpResponse.andExpect(status().isCreated())
        def samplePersonDto = extractResponseAsClass(httpResponse, SamplePersonDto)

        then:
        samplePersonDto == new SamplePersonDto(1L, name, surname)
    }

    def "invoke endpoint with default generic serivice, mappers, use default data storage with success"() {
        given:
        def createEndpointMetaModelDto = createValidPostExtendedUserWithValidators()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), createValidPerson())

        then:
        httpResponse.andExpect(status().isCreated())
        // TODO in future assert what was returned from datasource after save
    }

    def "invoke endpoint with default generic serivice, mappers, use default data storage with failure"() {
        given:
        def createEndpointMetaModelDto = createValidPostExtendedUserWithValidators()
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        when:
        def httpResponse = rawOperationsOnEndpoints.performWithJsonContent(MockMvcRequestBuilders.post("/users"), createInvalidPerson())
        httpResponse.andExpect(status().isBadRequest())
        def errorResponse = extractErrorResponseDto(httpResponse)

        then:
        assertValidationResults(errorResponse.getErrors(), [
            errorEntry("surname", notNullMessage(), NOT_NULL_MESSAGE_PROPERTY),
            errorEntry("name", invalidSizeMessage(2, 20), SIZE_MESSAGE_PROPERTY),
            errorEntry("documents", invalidSizeMessage(1, null), SIZE_MESSAGE_PROPERTY)
        ])
    }

    private static class ExampleUser {

        Long id
        String name
        String surname
    }

    private static ExtendedPerson createValidPerson() {
        new ExtendedPerson(
            id: 12, name: randomText(2), surname: randomText(30), documents: [
                new Document(type: 1, value: randomText(5))
            ]
        )
    }

    private static ExtendedPerson createInvalidPerson() {
        new ExtendedPerson(
            id: 12, name: randomText(22), documents: []
        )
    }

    private static class ExtendedPerson {

        Long id
        String name
        String surname
        LocalDate birthDate
        List<Document> documents
    }

    private static class Document {

        Long id
        Byte type
        String value
        LocalDate validFrom
        LocalDate validTo
    }
}
