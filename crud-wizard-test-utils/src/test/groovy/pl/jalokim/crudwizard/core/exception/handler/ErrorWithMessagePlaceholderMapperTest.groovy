package pl.jalokim.crudwizard.core.exception.handler

import static pl.jalokim.crudwizard.core.exception.handler.ErrorWithMessagePlaceholderMapper.convertToErrorsDto

import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder
import pl.jalokim.crudwizard.core.exception.ErrorWithMessagePlaceholder
import pl.jalokim.crudwizard.test.utils.random.DataFakerHelper
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl
import spock.lang.Specification

class ErrorWithMessagePlaceholderMapperTest extends Specification {

    def setup() {
        AppMessageSourceTestImpl.initStaticAppMessageSource()
    }

    def "should convert from ErrorDtoWithPlaceholder to ErrorDto with expectedWay"() {
        given:
        def property1 = DataFakerHelper.randomText()
        def property2 = DataFakerHelper.randomText()
        def property3 = DataFakerHelper.randomText()
        def rawMessage1 = DataFakerHelper.randomText()
        def rawMessage2 = DataFakerHelper.randomText()
        def fixedErrorCode1 = DataFakerHelper.randomText()
        def fixedErrorCode2 = DataFakerHelper.randomText()
        def fixedErrorCode3 = DataFakerHelper.randomText()

        def innerProperty = DataFakerHelper.randomText()
        def innerRawMessage = DataFakerHelper.randomText()
        def innerErrorCode = DataFakerHelper.randomText()

        Set<ErrorWithMessagePlaceholder> errorsDtoWithPlaceholder = Set.of(
            ErrorWithMessagePlaceholder.builder()
                .property(property1)
                .rawMessage(rawMessage1)
                .errorCode(fixedErrorCode1)
                .build(),
            ErrorWithMessagePlaceholder.builder()
                .property(property2)
                .rawMessage(rawMessage2)
                .errorCode(fixedErrorCode2)
                .messagePlaceholder(MessagePlaceholder.builder()
                    .mainPlaceholder("example.property")
                    .errorCode(DataFakerHelper.randomText())
                    .build())
                .errors(Set.of(ErrorWithMessagePlaceholder.builder()
                    .property(innerProperty)
                    .rawMessage(innerRawMessage)
                    .errorCode(innerErrorCode)
                    .build()))
                .build(),
            ErrorWithMessagePlaceholder.builder()
                .property(property3)
                .messagePlaceholder(MessagePlaceholder.builder()
                    .mainPlaceholder("example.property")
                    .errorCode(fixedErrorCode3)
                    .build())
                .build()
        )

        when:
        Map<String, ErrorDto> results = convertToErrorsDto(errorsDtoWithPlaceholder).collectEntries({
            [it.property, it]
        })
        then:
        verifyAll(results[property1]) {
            property == property1
            message == rawMessage1
            errorCode == fixedErrorCode1
        }
        verifyAll(results[property2]) {
            property == property2
            message == rawMessage2
            errorCode == fixedErrorCode2
        }
        verifyAll(results[property3]) {
            property == property3
            message == 'example value'
            errorCode == fixedErrorCode3
        }
    }
}
