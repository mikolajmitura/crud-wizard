package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.genericapp.customendpoint.SomeCustomRestController
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class EndpointMetaModelServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private EndpointMetaModelRepository endpointMetaModelRepository

    def "should save simple POST new endpoint with default mapper, service, data storage"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto()

        when:
        def createdId = endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.findExactlyOneById(createdId)
            verifyAll(endpointEntity) {
                verifyAll(apiTag) {
                    id != null
                    name == createEndpointMetaModelDto.apiTag.name
                }
                baseUrl == createEndpointMetaModelDto.baseUrl
                httpMethod == createEndpointMetaModelDto.httpMethod
                operationName == createEndpointMetaModelDto.operationName
                def inputPayloadMetamodel = createEndpointMetaModelDto.payloadMetamodel
                verifyAll(payloadMetamodel) {
                    name == inputPayloadMetamodel.name
                    fields.size() == 1
                    verifyAll(fields[0]) {
                        fieldName == inputPayloadMetamodel.fields[0].fieldName
                        verifyAll(fieldType) {
                            className == inputPayloadMetamodel.fields[0].fieldType.className
                        }
                    }
                }
                verifyAll(responseMetaModel) {
                    verifyAll(classMetaModel) {
                        name == null
                        className == createEndpointMetaModelDto.responseMetaModel.classMetaModel.className
                    }
                    successHttpCode == createEndpointMetaModelDto.responseMetaModel.successHttpCode
                }
            }
        }
    }

    def "should throw ConstraintViolationException when bean is invalid (verify only that aspect was invoked)"() {
        given:
        def createEndpointMetaModelDto = emptyEndpointMetaModelDto()

        when:
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        ConstraintViolationException tx = thrown()
        isNotEmpty(tx.constraintViolations)
    }

    def "should not save endpoint when it override custom endpoint from spring"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("some-endpoint/{someId}/second-part/{partId}")
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .fields([
                    createValidFieldMetaModelDto("someId", String),
                    createValidFieldMetaModelDto("partId", Long)
                ])
                .build())
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("", createMessagePlaceholder(EndpointNotExistsAlready, "springRestController", [
                url               : "some-endpoint/{someId}/second-part/{partId}",
                httpMethod        : "POST",
                restClassAndMethod: "$SomeCustomRestController.canonicalName#somePost(Long, String)"
            ]).translateMessage())
        ])
    }

    // TODO save all fields during create endpoint metamodels, queryArguments, responseMetaModel with not raw java class but with class metadata,
    //  classMetaModelInDataStorage other than payload

    // TODO new endpoint with already existed metamodels
    // TODO save with new service
    // TODO save with new mapper
    // TODO save with not default data storage
}
