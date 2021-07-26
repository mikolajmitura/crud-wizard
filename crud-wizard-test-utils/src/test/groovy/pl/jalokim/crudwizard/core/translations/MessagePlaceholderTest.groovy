package pl.jalokim.crudwizard.core.translations

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder

import pl.jalokim.crudwizard.core.exception.handler.DummyRestController
import pl.jalokim.utils.test.DataFakerHelper
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImplTest
import spock.lang.Specification

class MessagePlaceholderTest extends Specification {

    AppMessageSource appMessageSource = new AppMessageSourceTestImpl()

    def "should return expected values from MessagePlaceholder with only mainPlaceholder"() {
        given:
        def mainPlaceHolderText = DataFakerHelper.randomText()
        when:
        def messagePlaceholder = MessagePlaceholder.builder()
            .mainPlaceholder(mainPlaceHolderText)
            .build()
        then:
        verifyAll(messagePlaceholder) {
            mainPlaceholder == mainPlaceHolderText
            rawArguments == []
            errorCode == null
        }
    }

    def "should return expected values from MessagePlaceholder all fields"() {
        given:
        def mainPlaceHolderText = DataFakerHelper.randomText()
        def errorCodeText = DataFakerHelper.randomText()
        when:
        def messagePlaceholder = buildMessagePlaceholder(mainPlaceHolderText, errorCodeText)
        then:
        verifyAll(messagePlaceholder) {
            mainPlaceholder == mainPlaceHolderText
            rawArguments == [1, 2, "{some.property1}", "{some.property2}", "some text",
                             "{some.property3}", "{some.property4}"]
            errorCode == errorCodeText
        }
    }

    def "should return expected placeholders"() {
        when:
        def buildPlaceholder1 = wrapAsPlaceholder("node1", "node2", "node3")
        def buildPlaceholder2 = wrapAsPlaceholder(String, "node2")
        def buildPlaceholder3 = wrapAsPlaceholder(ExampleEnum.ENUM2)
        then:
        buildPlaceholder1 == "{node1.node2.node3}"
        buildPlaceholder2 == "{java.lang.String.node2}"
        buildPlaceholder3 == "{pl.jalokim.crudwizard.core.translations.ExampleEnum.ENUM2}"
    }

    def "should translateMessage from whole MessagePlaceholder"() {
        given:
        def messagePlaceholder = buildMessagePlaceholder("message.placeholder.test.some.code", DataFakerHelper.randomText())
        when:
        def translatedMessage = messagePlaceholder.translateMessage()
        then:
        translatedMessage == appMessageSource.getMessage("message.placeholder.test.some.code", 1, 2,
            appMessageSource.getMessage("some.property1"),
            appMessageSource.getMessage("some.property2"),
            "some text",
            appMessageSource.getMessage("some.property3"),
            appMessageSource.getMessage("some.property4"))
    }

    def "should return expected translatedMessage via shortcut method createMessagePlaceholder"() {
        when:
        def messagePlaceholder = createMessagePlaceholder(getClass(), "code.suffix",
            ExampleEnum.ENUM2, "raw value", "some.property1", "{some.property3}",
            MessagePlaceholder.builder()
                .mainPlaceholder(DummyRestController, "some.code")
                .argumentsAsPlaceholders("some.property1", "some.property2")
                .build()
        )
        then:
        messagePlaceholder.translateMessage() == "some message enum2 TEST raw value some.property1 some property 3 placeholders some property 1 some property 2"
        messagePlaceholder.getRawArguments() == [
            "{pl.jalokim.crudwizard.core.translations.ExampleEnum.ENUM2}", "raw value", "some.property1",
            "{some.property3}", "placeholders some property 1 some property 2"
        ]
    }

    def "should find property and change placeholders"() {
        when:
        def messagePlaceholder = createMessagePlaceholder("pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImplTest.placeholders",
            [anotherProperty: "{example.property}",
             rawValue       : "raw value",
             someEnum       : ExampleEnum.TEST
            ]
        )
        then:
        messagePlaceholder.translateMessage() == "example value example value raw value test:example value enum: TEST some"
    }

    def "should find property by class name and change placeholders"() {
        when:
        def messagePlaceholder = createMessagePlaceholder(AppMessageSourceTestImplTest.class, "placeholders",
            [anotherProperty: "{example.property}",
             rawValue       : "raw value",
             someEnum       : ExampleEnum.TEST
            ]
        )
        then:
        messagePlaceholder.translateMessage() == "example value example value raw value test:example value enum: TEST some"
    }

    def "should wrap text as external placeholder for fronted"() {
        given:
        def textToWrap = "some.property"
        when:
        def result = MessagePlaceholder.wrapAsExternalPlaceholder(textToWrap)
        then:
        result == "F#[${textToWrap}]#"
    }

    def "should wrap enum as external placeholder for fronted"() {
        when:
        def result = MessagePlaceholder.wrapAsExternalPlaceholder(ExampleEnum.ENUM2)
        then:
        result == "F_Enum#[example_enum.enum2]#"
    }

    static MessagePlaceholder buildMessagePlaceholder(String mainPlaceHolderText, String errorCodeText) {
        MessagePlaceholder.builder()
            .mainPlaceholder(mainPlaceHolderText)
            .errorCode(errorCodeText)
            .rawArguments(1, 2, wrapAsPlaceholder("some.property1"))
            .argumentsAsPlaceholders("some.property2")
            .rawArguments("some text")
            .argumentsAsPlaceholders("some.property3", "some.property4")
            .build()
    }
}
