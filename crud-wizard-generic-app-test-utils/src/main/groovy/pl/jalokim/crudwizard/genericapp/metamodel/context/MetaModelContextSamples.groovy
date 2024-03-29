package pl.jalokim.crudwizard.genericapp.metamodel.context

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpMethod.PUT
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPathParamsClassMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createQueryArgumentsMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSomePersonClassMetaModel

import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPart

class MetaModelContextSamples {

    static MetaModelContext createMetaModelContextWithOneEndpointInNodes() {
        MetaModelContext metaModelContext = new MetaModelContext()
        metaModelContext.getEndpointMetaModelContextNode()
            .putNextNodeOrGet(UrlPart.normalUrlPart("users"))
            .putNextNodeOrGet(UrlPart.variableUrlPart("userId"))
            .putNextNodeOrGet(UrlPart.normalUrlPart("orders"))
            .putNextNodeOrGet(UrlPart.variableUrlPart("orderId"))
            .putEndpointByMethod(EndpointMetaModel.builder()
                .urlMetamodel(UrlModelResolver.resolveUrl("users/{usersIdVar}/orders/{ordersIdVar}"))
                .httpMethod(POST)
                .payloadMetamodel(createSomePersonClassMetaModel())
                .queryArguments(createQueryArgumentsMetaModel())
                .pathParams(createPathParamsClassMetaModel())
                .operationName("existOperationName")
                .build())
        metaModelContext
    }

    static MetaModelContext createMetaModelContextWithEndpoints() {
        MetaModelContext metaModelContext = new MetaModelContext()
        def endpointMetaModels = new ModelsCache<EndpointMetaModel>()

        [
            newEndpointMetaModel(1, "users/{userId}", GET),
            newEndpointMetaModel(2, "users", POST),
            newEndpointMetaModel(3, "users/{userId}", PUT),
            newEndpointMetaModel(4, "users/{userId}/orders", POST),
            newEndpointMetaModel(5, "users/{userIdent}/orders/{orderId}", PUT),
            newEndpointMetaModel(6, "users/{userIdent}/orders/{orderId}", GET),
            newEndpointMetaModel(7, "users/{userIdent}/orders/{order}/reject", POST),
            newEndpointMetaModel(8, "users/report/orders/{order}/reject", POST),
            newEndpointMetaModel(9, "invoices/{invoice}/send", POST)
        ].each {endpointMetaModel ->
            endpointMetaModels.put(endpointMetaModel.getId(), endpointMetaModel)
        }
        metaModelContext.setEndpointMetaModels(endpointMetaModels)

        metaModelContext
    }

    static EndpointMetaModel newEndpointMetaModel(Long id, String rawUrl, HttpMethod httpMethod) {
        EndpointMetaModel.builder()
            .id(id)
            .urlMetamodel(UrlModelResolver.resolveUrl(rawUrl))
            .httpMethod(httpMethod)
            .build()
    }
}
