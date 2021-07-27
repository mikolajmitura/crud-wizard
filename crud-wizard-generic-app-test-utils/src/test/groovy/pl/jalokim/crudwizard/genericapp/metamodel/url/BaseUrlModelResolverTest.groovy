package pl.jalokim.crudwizard.genericapp.metamodel.url

import static pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPartTest.normalUrlPart
import static pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPartTest.variableUrlPart

import spock.lang.Specification
import spock.lang.Unroll

class BaseUrlModelResolverTest extends Specification {

    @Unroll
    def "build expected UrlMetamodel instance: #expectedUrlMetamodel from url: #baseUrl"() {
        when:
        def urlMetaModel = BaseUrlModelResolver.resolveUrl(baseUrl)

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
