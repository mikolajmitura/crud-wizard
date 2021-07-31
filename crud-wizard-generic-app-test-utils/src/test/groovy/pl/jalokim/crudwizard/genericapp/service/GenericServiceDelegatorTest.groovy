package pl.jalokim.crudwizard.genericapp.service

import static org.springframework.http.HttpMethod.POST
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createHttpQueryParamsTranslated
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBodyTranslated
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage

import javax.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.service.GenericServiceArgumentSamples
import pl.jalokim.crudwizard.genericapp.service.invoker.DelegatedServiceMethodInvoker
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.service.translator.RawEntityObjectTranslator
import pl.jalokim.crudwizard.genericapp.validation.generic.GenericValidator
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class GenericServiceDelegatorTest extends UnitTestSpec {

    MetaModelContextService metaModelContextService = Mock()
    DelegatedServiceMethodInvoker delegatedServiceMethodInvoker = Mock()
    RawEntityObjectTranslator rawEntityObjectTranslator = Mock()
    GenericValidator genericValidator = Mock()
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(jsonObjectMapper, metaModelContextService)
    HttpServletRequest request = Mock()

    GenericServiceDelegator testCase = new GenericServiceDelegator(delegatedServiceMethodInvoker, endpointMetaModelContextNodeUtils,
        rawEntityObjectTranslator, genericValidator)

    def "should find EndpointMetaModel in metamodel context"() {
        given:
        def genericServiceArgument = GenericServiceArgumentSamples.createInputGenericServiceArgument(request)
        def metaModelContext = MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes()
        metaModelContextService.getMetaModelContext() >> metaModelContext
        request.getMethod() >> "POST"
        request.getRequestURI() >> "/users/12/orders/14"

        when:
        def newGenericServiceArgument = testCase.searchForEndpointByRequest(genericServiceArgument)

        then:
        verifyAll(newGenericServiceArgument) {
            urlPathParams == [
                usersIdVar : "12",
                ordersIdVar: 14,
            ]
            endpointMetaModel == endpointMetaModelContextNodeUtils
                .findEndpointByUrl("/users/{userId}/orders/{orderId}", POST)
                .getEndpointMetaModel()
        }
    }

    @Unroll
    def "should not find EndpointMetaModel in metamodel context"() {
        given:
        def genericServiceArgument = GenericServiceArgumentSamples.createInputGenericServiceArgument(request)
        def metaModelContext = MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes()
        metaModelContextService.getMetaModelContext() >> metaModelContext
        request.getMethod() >> httpMethod
        request.getRequestURI() >> requestedUrl

        when:
        testCase.searchForEndpointByRequest(genericServiceArgument)

        then:
        EntityNotFoundException ex = thrown()
        ex.message == getMessage("error.url.not.found", requestedUrl)

        where:
        requestedUrl          | httpMethod
        "/users/12/orders/14" | "PUT"
        "/users/12/orders/14" | "GET"
        "/users/12/orders"    | "POST"
    }

    def "should find endpoint translate and invoke validation on them"() {
        given:
        def genericServiceArgument = GenericServiceArgumentSamples.createInputGenericServiceArgument(request)
        def metaModelContext = MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes()
        metaModelContextService.getMetaModelContext() >> metaModelContext
        request.getMethod() >> "POST"
        request.getRequestURI() >> "/users/12/orders/14"
        def expectedResponseEntity = ResponseEntity.created().body(12)
        GenericServiceArgument passedGenericServiceArgument = null

        def foundEndpointMetaModel = endpointMetaModelContextNodeUtils
            .findEndpointByUrl("/users/{userId}/orders/{orderId}", POST)
            .getEndpointMetaModel()

        def expectedRequestBodyTranslated = createRequestBodyTranslated()
        rawEntityObjectTranslator.translateToRealObjects(genericServiceArgument.getRequestBody(),
            foundEndpointMetaModel.getPayloadMetamodel()) >> expectedRequestBodyTranslated
        def expectedHttpQueryParamsTranslated = createHttpQueryParamsTranslated()
        rawEntityObjectTranslator.translateToRealObjects(genericServiceArgument.getHttpQueryParams(),
            foundEndpointMetaModel.getQueryArguments()) >> expectedHttpQueryParamsTranslated

        boolean validatedHttpParams = false
        boolean validatedPayload = false

        genericValidator.validate(_ as Map<String, Object>, _ as ClassMetaModel) >> { args ->
            if (expectedHttpQueryParamsTranslated == args[0]
                && foundEndpointMetaModel.queryArguments == args[1]) {
                validatedHttpParams = true
            }

            if (expectedRequestBodyTranslated == args[0]
                && foundEndpointMetaModel.payloadMetamodel == args[1]) {
                validatedPayload = true
            }
        }

        when:
        def responseEntity = testCase.findAndInvokeHttpMethod(genericServiceArgument)

        then:
        responseEntity == expectedResponseEntity
        delegatedServiceMethodInvoker.invokeMethod(_ as GenericServiceArgument) >> {args ->
            passedGenericServiceArgument = args[0]
            expectedResponseEntity
        }

        verifyAll(passedGenericServiceArgument) {
            verifyAll(endpointMetaModel) {
                urlPathParams == [
                    usersIdVar : "12",
                    ordersIdVar: 14,
                ]
                endpointMetaModel == foundEndpointMetaModel
            }
            requestBodyTranslated == expectedRequestBodyTranslated
            httpQueryTranslated == expectedHttpQueryParamsTranslated
        }

        validatedHttpParams
        validatedPayload
    }
}
