package pl.jalokim.crudwizard.core.validation.javax.groups

import static org.hamcrest.MatcherAssert.assertThat
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
        def response = performWithJsonContent(post("/test/create"), input)
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
        def response = performWithJsonContent(post("/test/create"), input)
        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = extractResponseAsJson(response)
        jsonResponse['errors'].size() == 4
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('surName', 'must not be null'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('pesel', 'must not be null'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someBean.surName', 'must not be null'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someBean.pesel', 'must not be null'))
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
        def response = performWithJsonContent(post("/test/update"), input)
        then:
        response.andExpect(status().isOk())
    }

    def "should not pass validation update"() {
        given:

        def input = SomeBean.builder()
            .surName("surName")
            .optional("optinal")
            .build()

        when:
        def response = performWithJsonContent(post("/test/update"), input)
        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = extractResponseAsJson(response)
        jsonResponse['errors'].size() == 2
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('id', 'must not be null'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('name', 'must not be null'))
    }
}
