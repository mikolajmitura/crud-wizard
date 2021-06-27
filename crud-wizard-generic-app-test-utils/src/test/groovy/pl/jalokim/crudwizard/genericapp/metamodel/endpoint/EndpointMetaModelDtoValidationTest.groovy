package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomText
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidMinMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class EndpointMetaModelDtoValidationTest extends UnitTestSpec {

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    @Unroll
    def "should return expected messages for default context of EndpointMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(endpointMetaModelDto)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        endpointMetaModelDto                  | expectedErrors
        createValidPostEndpointMetaModelDto() | []

        createValidPostEndpointMetaModelDto().toBuilder()
            .serviceMetaModel(createValidServiceMetaModelDto())
            .build()                          | []

        emptyEndpointMetaModelDto()           | [
            errorEntry("apiTag", notNullMessage()),
            errorEntry("baseUrl", notNullMessage()),
            errorEntry("httpMethod", notNullMessage()),
            errorEntry("operationName", notNullMessage())
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(null)
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("responseMetaModel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "POST"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.PUT)
            .responseMetaModel(null)
            .payloadMetamodel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.PATCH)
            .responseMetaModel(null)
            .payloadMetamodel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.GET)
            .payloadMetamodel(createEmptyClassMetaModelDto())
            .queryArguments([createEmptyClassMetaModelDto()])
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "DELETE"]
            )),
            errorEntry("responseMetaModel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "POST"]
            )),
            errorEntry("payloadMetamodel.name", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "className", NULL, []
            )),
            errorEntry("queryArguments[0].name", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "className", NULL, []
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.DELETE)
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "DELETE"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .serviceMetaModel(
                createValidServiceMetaModelDtoAsScript().toBuilder()
                    .beanName(randomText())
                    .build()
            )
            .build()                          | [
            errorEntry("serviceMetaModel.beanName", fieldShouldWhenOtherMessage(
                NULL, [], "serviceScript", NOT_NULL, []
            )),
            errorEntry("serviceMetaModel.serviceScript", fieldShouldWhenOtherMessage(
                NULL, [], "beanName", NOT_NULL, []
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createEmptyClassMetaModelDto())
                .successHttpCode(10)
                .build()
            )
            .build()                          | [
            errorEntry("responseMetaModel.classMetaModel.name", fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])),
            errorEntry("responseMetaModel.successHttpCode", invalidMinMessage(100))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createValidClassMetaModelDtoWithClassName())
                .build()
            ).build()                         | []

        createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(DataStorageMetaModelDto.builder().build())
                    .mapperMetaModel(MapperMetaModelDto.builder()
                        .className(randomText())
                        .beanName(randomText())
                        .methodName(randomText())
                        .mapperScript(randomText())
                        .build())
                    .classMetaModelInDataStorage(createEmptyClassMetaModelDto())
                    .build()
            ])
            .build()                          | [
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.name", fieldShouldWhenOtherMessage(NOT_NULL, [], "id", NULL, [])),
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.className", fieldShouldWhenOtherMessage(NOT_NULL, [], "id", NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.className", fieldShouldWhenOtherMessage(NULL, [], "mapperScript", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.beanName", fieldShouldWhenOtherMessage(NULL, [], "mapperScript", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.methodName", fieldShouldWhenOtherMessage(NULL, [], "mapperScript", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.mapperScript", fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.mapperScript", fieldShouldWhenOtherMessage(NULL, [], "beanName", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.mapperScript", fieldShouldWhenOtherMessage(NULL, [], "methodName", NOT_NULL, [])),
            errorEntry("dataStorageConnectors[0].mapperMetaModel.mappingDirection", notNullMessage()),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage.name", fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])),
        ]
    }

    @Unroll
    def "should return expected messages for update context of EndpointMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(endpointMetaModelDto, EndpointUpdateContext)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        endpointMetaModelDto           | expectedErrors
        createValidPostEndpointMetaModelDto()
            .toBuilder().id(1).build() | []
        emptyEndpointMetaModelDto()    | [
            errorEntry("id", notNullMessage()),
            errorEntry("apiTag", notNullMessage()),
            errorEntry("baseUrl", notNullMessage()),
            errorEntry("httpMethod", notNullMessage()),
            errorEntry("operationName", notNullMessage())
        ]
    }

    private static EndpointMetaModelDto emptyEndpointMetaModelDto() {
        EndpointMetaModelDto.builder().build()
    }

}
