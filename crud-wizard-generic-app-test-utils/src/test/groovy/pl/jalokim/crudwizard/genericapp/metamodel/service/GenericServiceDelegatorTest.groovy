package pl.jalokim.crudwizard.genericapp.metamodel.service

import static org.springframework.http.HttpMethod.POST
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModel

import com.fasterxml.jackson.databind.ObjectMapper
import javax.servlet.http.HttpServletRequest
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver
import pl.jalokim.crudwizard.genericapp.service.DelegatedServiceMethodInvoker
import pl.jalokim.crudwizard.genericapp.service.GenericServiceDelegator
import pl.jalokim.crudwizard.test.utils.UnitTestSpec

class GenericServiceDelegatorTest extends UnitTestSpec {

    MetaModelContextService metaModelContextService = Mock()
    DelegatedServiceMethodInvoker delegatedServiceMethodInvoker = Mock()
    ObjectMapper objectMapper = createObjectMapper()
    HttpServletRequest request = Mock()

    GenericServiceDelegator testCase = new GenericServiceDelegator(metaModelContextService, delegatedServiceMethodInvoker, objectMapper)

    def "should find EndpointMetaModel in metamodel context"() {
        given:
        def genericServiceArgument = GenericServiceArgumentSamples.createInputGenericServiceArgument(request)
        def metaModelContext = MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes()
        request.getMethod() >> "POST"
        request.getRequestURI() >> "/users/12/orders/14"

        when:
        def newGenericServiceArgument = testCase.searchForEndpointByRequest(genericServiceArgument, metaModelContext)

        then:
        newGenericServiceArgument.urlPathParams == RawEntityObject.fromMap([
            usersIdVar: "12",
            ordersIdVar: 14,
        ])
        newGenericServiceArgument.endpointMetaModel == EndpointMetaModel.builder()
                .urlMetamodel(UrlModelResolver.resolveUrl("users/{usersIdVar}/orders/{ordersIdVar}"))
                .httpMethod(POST)
                .pathParams(ClassMetaModel.builder()
                    .name("pathParamsMeta")
                    .fields([
                        createValidFieldMetaModel("usersIdVar", String),
                        createValidFieldMetaModel("ordersIdVar", Long)
                    ])
                    .build())
                .operationName("existOperationName")
                .build()
    }

    // TODO #02 should not find EndpointMetaModel in metamodel context with @Unroll
}
