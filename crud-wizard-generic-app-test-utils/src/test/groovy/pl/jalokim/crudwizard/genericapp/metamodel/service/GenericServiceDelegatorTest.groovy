package pl.jalokim.crudwizard.genericapp.metamodel.service

import static org.springframework.http.HttpMethod.POST
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage

import javax.servlet.http.HttpServletRequest
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.service.DelegatedServiceMethodInvoker
import pl.jalokim.crudwizard.genericapp.service.GenericServiceDelegator
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class GenericServiceDelegatorTest extends UnitTestSpec {

    MetaModelContextService metaModelContextService = Mock()
    DelegatedServiceMethodInvoker delegatedServiceMethodInvoker = Mock()
    EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(createObjectMapper(), metaModelContextService)
    HttpServletRequest request = Mock()

    GenericServiceDelegator testCase = new GenericServiceDelegator(delegatedServiceMethodInvoker, endpointMetaModelContextNodeUtils)

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
        newGenericServiceArgument.urlPathParams == RawEntityObject.fromMap([
            usersIdVar : "12",
            ordersIdVar: 14,
        ])
        newGenericServiceArgument.endpointMetaModel == endpointMetaModelContextNodeUtils
            .findEndpointByUrl("/users/{userId}/orders/{orderId}", POST)
            .getEndpointMetaModel()
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
}
