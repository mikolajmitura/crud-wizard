package pl.jalokim.crudwizard.genericapp.metamodel.context

import static pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNode.VARIABLE_URL_PART

import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.url.BaseUrlModelResolver
import spock.lang.Specification

class EndpointMetaModelContextNodeCreatorTest extends Specification {

    def "should return expected url nodes"() {
        given:
        MetaModelContext metaModelContext = new MetaModelContext()
        def endpointMetaModels = new ModelsCache<EndpointMetaModel>()

        [
            newEndpointMetaModel(1, "users/{userId}", HttpMethod.GET),
            newEndpointMetaModel(2, "users", HttpMethod.POST),
            newEndpointMetaModel(3, "users/{userId}", HttpMethod.PUT),
            newEndpointMetaModel(4, "users/{userId}/orders", HttpMethod.POST),
            newEndpointMetaModel(5, "users/{userIdent}/orders/{orderId}", HttpMethod.PUT),
            newEndpointMetaModel(6, "users/{userIdent}/orders/{orderId}", HttpMethod.GET),
            newEndpointMetaModel(7, "users/{userIdent}/orders/{order}/reject", HttpMethod.POST),
            newEndpointMetaModel(8, "users/report/orders/{order}/reject", HttpMethod.POST),
            newEndpointMetaModel(9, "invoices/{invoice}/send", HttpMethod.POST)
        ].each {endpointMetaModel ->
            endpointMetaModels.put(endpointMetaModel.getId(), endpointMetaModel)
        }

        metaModelContext.setEndpointMetaModels(endpointMetaModels)

        when:
        EndpointMetaModelContextNodeCreator.loadEndpointNodes(metaModelContext)

        then:
        def endpointMetaModelContextNode = metaModelContext.endpointMetaModelContextNode
        endpointMetaModelContextNode.urlPart.originalValue == "/"
        endpointMetaModelContextNode.nextNodesByPath.size() == 2

        // users
        def usersNode = endpointMetaModelContextNode.nextNodesByPath["users"]
        usersNode.endpointsByHttpMethod.size() == 1
        usersNode.endpointsByHttpMethod[HttpMethod.POST].id == 2
        usersNode.nextNodesByPath.size() == 2
        usersNode.urlPart.originalValue == "users"

        def usersVariableNode = usersNode.nextNodesByPath[VARIABLE_URL_PART]
        usersVariableNode.nextNodesByPath.size() == 1
        usersVariableNode.endpointsByHttpMethod.size() == 2
        usersVariableNode.endpointsByHttpMethod[HttpMethod.GET].id == 1
        usersVariableNode.endpointsByHttpMethod[HttpMethod.PUT].id == 3

        def ordersInUsersNode = usersVariableNode.nextNodesByPath["orders"]
        ordersInUsersNode.endpointsByHttpMethod.size() == 1
        ordersInUsersNode.endpointsByHttpMethod[HttpMethod.POST].id == 4
        ordersInUsersNode.nextNodesByPath.size() == 1

        def orderIdVariableNode = ordersInUsersNode.nextNodesByPath[VARIABLE_URL_PART]
        orderIdVariableNode.endpointsByHttpMethod.size() == 2
        orderIdVariableNode.endpointsByHttpMethod[HttpMethod.PUT].id == 5
        orderIdVariableNode.endpointsByHttpMethod[HttpMethod.GET].id == 6
        orderIdVariableNode.nextNodesByPath.size() == 1

        def rejectInOrdersNode = orderIdVariableNode.nextNodesByPath["reject"]
        rejectInOrdersNode.endpointsByHttpMethod.size() == 1
        rejectInOrdersNode.endpointsByHttpMethod[HttpMethod.POST].id == 7
        rejectInOrdersNode.nextNodesByPath.isEmpty()

        def usersReportNode = usersNode.nextNodesByPath["report"]
        def rejectReportNode = usersReportNode.nextNodesByPath["orders"]
            .nextNodesByPath[VARIABLE_URL_PART]
            .nextNodesByPath["reject"]

        rejectReportNode.endpointsByHttpMethod[HttpMethod.POST].id == 8
        rejectReportNode.endpointsByHttpMethod[HttpMethod.PUT] == null

        // invoices
        def invoicesNode = endpointMetaModelContextNode.nextNodesByPath["invoices"]
        invoicesNode.urlPart.originalValue == "invoices"
        invoicesNode.endpointsByHttpMethod.isEmpty()

        def invoiceVariableNode = invoicesNode.nextNodesByPath[VARIABLE_URL_PART]
        invoiceVariableNode.urlPart.variableName == VARIABLE_URL_PART
        invoiceVariableNode.endpointsByHttpMethod.isEmpty()

        def invoiceSendNode = invoiceVariableNode.nextNodesByPath["send"]
        invoiceSendNode.urlPart.originalValue == "send"
        invoiceSendNode.endpointsByHttpMethod.size() == 1
        invoiceSendNode.endpointsByHttpMethod[HttpMethod.POST].id == 9
        invoiceSendNode.nextNodesByPath.isEmpty()
    }

    private static EndpointMetaModel newEndpointMetaModel(Long id, String rawUrl, HttpMethod httpMethod) {
        EndpointMetaModel.builder()
            .id(id)
            .urlMetamodel(BaseUrlModelResolver.resolveUrl(rawUrl))
            .httpMethod(httpMethod)
            .build()
    }
}