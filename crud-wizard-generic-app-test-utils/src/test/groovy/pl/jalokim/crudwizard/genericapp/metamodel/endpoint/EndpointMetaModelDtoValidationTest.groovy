package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPutEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidMinMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.context.ApplicationContext
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.PathParamsAndUrlVariablesTheSame
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class EndpointMetaModelDtoValidationTest extends UnitTestSpec {

    private MetaModelContextService metaModelContextService = Mock()
    private ApplicationContext applicationContext = Mock()
    private jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    private endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(jsonObjectMapper, metaModelContextService)
    private validatorWithConverter = createValidatorWithConverter(endpointMetaModelContextNodeUtils, applicationContext)

    def setup() {
        RequestMappingHandlerMapping abstractHandlerMethodMapping = Mock()
        applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class) >> abstractHandlerMethodMapping
        abstractHandlerMethodMapping.getHandlerMethods() >> [:]
    }

    @Unroll
    def "should return expected messages for default context of EndpointMetaModelDto"() {
        given:
        MetaModelContext metaModelContext = createMetaModelContextWithOneEndpointInNodes()

        metaModelContextService.getMetaModelContext() >> metaModelContext

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
            .pathParams(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("pathParams", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["PUT", "PATCH"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.PATCH)
            .responseMetaModel(null)
            .payloadMetamodel(null)
            .pathParams(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("pathParams", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["PUT", "PATCH"]
            ))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.GET)
            .payloadMetamodel(createEmptyClassMetaModelDto())
            .queryArguments(createEmptyClassMetaModelDto())
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
            errorEntry("queryArguments.name", fieldShouldWhenOtherMessage(
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

        createValidPutEndpointMetaModelDto()  | []

        createValidPutEndpointMetaModelDto().toBuilder()
            .baseUrl("base-path/{basePath}/next-url")
            .build()                          | [
            errorEntry("", messageForValidator(PathParamsAndUrlVariablesTheSame, [
                baseUrl: "base-path/{basePath}/next-url",
                fieldName: wrapAsExternalPlaceholder("pathParams"),
                fieldNames: "basePath, nextId"
            ]))
        ]

        createValidPutEndpointMetaModelDto().toBuilder()
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("basePath", Double),
                    FieldMetaModelDto.builder()
                        .fieldName("nextId")
                        .fieldType(createValidClassMetaModelDtoWithName())
                        .build()

                ])
                .build())
            .build()                          | [
            errorEntry("", getMessage(PathParamsAndUrlVariablesTheSame, "allFieldsShouldHasClassName"))
        ]

        createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("users/{userId}/orders/{orderId}")
            .build() | [
            errorEntry("", createMessagePlaceholder(EndpointNotExistsAlready, "crudWizardController", [
                url: "users/{userId}/orders/{orderId}",
                httpMethod: "POST",
                foundUrl: "users/{usersIdVar}/orders/{ordersIdVar}",
                foundOperationName: "existOperationName",
            ]).translateMessage()),
            errorEntry("", messageForValidator(PathParamsAndUrlVariablesTheSame, [
                baseUrl   : "users/{userId}/orders/{orderId}",
                fieldName: wrapAsExternalPlaceholder("pathParams"),
                fieldNames: ""
            ]))
        ]
    }

    @Unroll
    def "should return expected messages for update context of EndpointMetaModelDto"() {
        given:
        MetaModelContext metaModelContext = new MetaModelContext()
        metaModelContextService.getMetaModelContext() >> metaModelContext

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
}
