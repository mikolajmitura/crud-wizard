package pl.jalokim.crudwizard.core.exception.handler

import static org.hamcrest.MatcherAssert.assertThat
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder
import static pl.jalokim.crudwizard.test.utils.rest.ErrorResponseMatcher.findErrorsDtoByProperty
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidPatternMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.resourceAlreadyUpdatedMessage

import pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import pl.jalokim.crudwizard.test.utils.rest.ErrorResponseMatcher

class GlobalExceptionHandlerIT extends DummyBaseIntegrationControllerSpec {

    def "should handle constraint violation exceptions and return response containing validation messages"() {
        given:
        DummyDto request = new DummyDto(null, '%$@#$(*&!@#!@^ACA', 6, 1, 2, List.of(
            InnerDummyDto.builder()
                .someLong(16)
                .innerDummyDto1(InnerDummyDto.builder()
                    .innerDummyDto2(
                        InnerDummyDto.builder()
                            .anotherList(List.of())
                            .build()
                    )
                    .build())
                .build(),
            InnerDummyDto.builder().someLong(4).build(),
            InnerDummyDto.builder().someLong(11).build(),
        ), "invalid")

        when:
        def response = operationsOnRestController.performWithJsonContent(post("/test"), request)

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains 'Invalid request'
        jsonResponse['errors'].size() == 11
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('mandatoryField', notNullMessage()))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('alphanumericString', invalidPatternMessage('^[A-Za-z0-9]*\$')))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('firstLong', "custom validation message ENUM2"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('secondLong', "custom validation message enum2 TEST"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('thirdLong', "some custom validation message"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('thirdLong', "thirdLong message"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someList[0].someLong', "higher than 5 index: 0"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someList[2].someLong', "higher than 5 index: 2"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('someList[0].innerDummyDto1.innerDummyDto2.anotherList', "long property message"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('',
            "field ${wrapAsExternalPlaceholder('some.field1')} and field ${wrapAsExternalPlaceholder('some.field2')}"))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('thirdLong',
            "field ${wrapAsExternalPlaceholder('some.field1')} and field ${wrapAsExternalPlaceholder('some.field2')}"))
    }

    def "should handle entity not found exceptions and return response exception messages"() {
        when:
        def response = operationsOnRestController.perform(get("/test/modelnotfound"))

        then:
        response.andExpect(status().isNotFound())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains 'My entity was not found with id: id'
    }

    def "should handle BindException and return response containing binding errors"() {
        when:
        def response = operationsOnRestController.perform(get("/test/bindexception"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains 'Invalid request'
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('objectA', 'An error has occurred'))
    }

    def "should handle ResourceChangedException and return response containing exception message"() {
        when:
        def response = operationsOnRestController.perform(get("/test/optimisticLocking"))

        then:
        response.andExpect(status().isConflict())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains resourceAlreadyUpdatedMessage()
    }

    def "should handle BusinessLogicException and rewrite exception message as error"() {
        when:
        def response = operationsOnRestController.perform(get("/test/businessLogicException"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains 'Business logic exception'
    }

    def "should handle TechnicalException and rewrite exception message as error"() {
        when:
        def response = operationsOnRestController.perform(get("/test/technicalException"))

        then:
        response.andExpect(status().isNotImplemented())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'].contains 'Technical exception'
    }

    def "should handle DataValidationException and rewrite exception message as error"() {
        when:
        def response = operationsOnRestController.perform(get("/test/data-validation-exception"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'] == "some error"
        jsonResponse['errors'].size() == 3
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('property1', 'message 1'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('property2', 'message 2'))
        assertThat(jsonResponse, ErrorResponseMatcher.hasError('property3', 'example value'))
        findErrorsDtoByProperty(jsonResponse, "property3")[0].errorCode == "error.code.from.placeholder"
    }

    def "should handle raw Exception and rewrite exception message as error"() {
        when:
        def response = operationsOnRestController.perform(get("/test/raw-exception"))

        then:
        response.andExpect(status().isInternalServerError())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'] == "raw exception message"
        jsonResponse['errors'] == null
    }

    def "should handle instance of ApplicationException and not translate exception"() {
        when:
        def response = operationsOnRestController.perform(get("/test/throw-exception-without-text-placeholder"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'] == "not translated message"
        jsonResponse['errorCode'] == null
    }

    def "should handle instance of ApplicationException and translate exception message"() {
        when:
        def response = operationsOnRestController.perform(get("/test/throw-exception-with-text-placeholder"))

        then:
        response.andExpect(status().isNotFound())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'] == "Some global exception"
        jsonResponse['errorCode'] == "some.global.exception.message"
    }

    def "should handle instance of ApplicationException with MessagePlaceholder instance"() {
        when:
        def response = operationsOnRestController.perform(get("/test/throw-exception-with-message-placeholder-object"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJson(response)
        jsonResponse['message'] == "placeholders some property 1 12"
        jsonResponse['errorCode'] == "fixed.error.code"
    }

    def "should return passed offset via header"() {
        when:
        def response = operationsOnRestController.perform(get("/test/required-time-zone-header")
            .header(TimeZoneRequestHolder.X_TIMEZONE_NAME_HEADER, "Europe/Warsaw"))

        then:
        response.andExpect(status().isOk())
        def jsonResponse = operationsOnRestController.extractResponseAsJsonObject(response)
        jsonResponse[0] == "Europe/Warsaw"
    }

    def "should return that not passed offset via header"() {
        when:
        def response = operationsOnRestController.perform(get("/test/required-time-zone-header"))

        then:
        response.andExpect(status().isBadRequest())
        def jsonResponse = operationsOnRestController.extractResponseAsJsonObject(response)
        jsonResponse['message'] == "required ${TimeZoneRequestHolder.X_TIMEZONE_NAME_HEADER} header"
    }
}
