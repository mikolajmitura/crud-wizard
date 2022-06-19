package pl.jalokim.crudwizard.genericapp.metamodel.url

import static UrlPart.normalUrlPart
import static UrlPart.variableUrlPart

import spock.lang.Specification
import spock.lang.Unroll

class UrlModelResolverTest extends Specification {

    @Unroll
    def "build expected UrlMetamodel instance: #expectedUrlMetamodel from url: #baseUrl"() {
        when:
        def urlMetaModel = UrlModelResolver.resolveUrl(baseUrl)

        then:
        urlMetaModel.rawUrl == baseUrl
        urlMetaModel.urlParts == expectedUrlParts

        where:
        baseUrl                            || expectedUrlParts
        "/users/{userId}"                  || [normalUrlPart("users"), variableUrlPart("userId")]
        "/users/{userId}/orders/{orderId}" || [normalUrlPart("users"), variableUrlPart("userId"),
                                               normalUrlPart("orders"), variableUrlPart("orderId")]
        "/users/data"                      || [normalUrlPart("users"), normalUrlPart("data")]
        "/users//data"                     || [normalUrlPart("users"), normalUrlPart("data")]
    }
}
