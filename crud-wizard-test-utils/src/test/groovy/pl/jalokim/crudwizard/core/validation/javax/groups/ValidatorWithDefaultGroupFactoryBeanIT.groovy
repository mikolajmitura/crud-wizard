package pl.jalokim.crudwizard.core.validation.javax.groups

import static org.hamcrest.MatcherAssert.assertThat
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage

import pl.jalokim.crudwizard.core.exception.handler.SomeBean
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import pl.jalokim.crudwizard.test.utils.rest.ErrorResponseMatcher

class ValidatorWithDefaultGroupFactoryBeanIT extends DummyBaseIntegrationControllerSpec {

    def "should pass validation create"() {
        given:
        def input = SomeBean.builder()
            .name("name")
            .surName("surName")
            .pesel("pesel")
            .first("test")
            .someBean(SomeBean.builder()
                .name("name1")
                .surName("surName1")
                .first("test")
                .pesel("pesel2").build())
            .build()

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test/create"), input)
        then:
        response.andExpect(status().isOk())
    }

    def "should not pass validation create"() {
        given:
        def input = SomeBean.builder()
            .name("name")
            .someBean(SomeBean.builder().name("test").build())
            .build()

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test/create"), input)
        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['errors'].size() == 4
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('surName', notNullMessage()))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('pesel', notNullMessage()))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someBean.surName', notNullMessage()))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someBean.pesel', notNullMessage()))
    }

    def "should pass validation update"() {
        given:

        def input = SomeBean.builder()
            .id(12)
            .name("name")
            .second("second")
            .surName("surName")
            .build()

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test/update"), input)
        then:
        response.andExpect(status().isOk())
    }

    def "should not pass validation update with UpdateContext and default validation group"() {
        given:

        def input = SomeBean.builder()
            .surName("surName")
            .optional("optinal")
            .build()

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test/update"), input)
        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['errors'].size() == 2
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('id', notNullMessage()))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('name', notNullMessage()))
    }

    def "should not pass validation update only with UpdateContext when added WithoutDefaultGroup"() {
        given:

        def input = SomeBean.builder()
            .surName("surName")
            .optional("optinal")
            .build()

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test/update-without-default-group"), input)
        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['errors'].size() == 1
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('id', notNullMessage()))
    }
}
