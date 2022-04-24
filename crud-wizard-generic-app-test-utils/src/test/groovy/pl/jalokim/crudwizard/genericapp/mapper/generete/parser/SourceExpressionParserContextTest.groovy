package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.assertException

import lombok.Getter
import org.springframework.context.ApplicationContext
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.test.utils.UnitTestSpec

class SourceExpressionParserContextTest extends UnitTestSpec {

    MapperConfigurationParserContext mapperConfigurationParserContext
    SourceExpressionParserContext sourceExpressionParserContext

    ApplicationContext applicationContext = Mock()
    MapperGenerateConfiguration mapperGenerateConfiguration = Mock()

    def setup() {
        mapperConfigurationParserContext = new MapperConfigurationParserContext(applicationContext, mapperGenerateConfiguration)
        mapperConfigurationParserContext.setInitColumnNumber(4)

        sourceExpressionParserContext = new SourceExpressionParserContext("  @   555  123cvx.gfa)h",
            mapperConfigurationParserContext, null, null)
    }

    def "MapperConfigurationParserContext returns expected values on methods"() {
        when:
        sourceExpressionParserContext.skipSpaces()

        then:
        sourceExpressionParserContext.getCurrentCharIndex() == 2
        mapperConfigurationParserContext.getColumnNumber() == 6
        sourceExpressionParserContext.getCurrentChar() == '@' as char
        sourceExpressionParserContext.skipSpaces()
        sourceExpressionParserContext.getCurrentChar() == '@' as char
        assertException({
            sourceExpressionParserContext.nextCharIs("." as char)
        }).thenNestedException(createExpectedException(7, createMessagePlaceholder("expected.any.of.chars", ".")))
        sourceExpressionParserContext.moveToPreviousChar()

        and:
        sourceExpressionParserContext.moveToPreviousChar()

        then:
        sourceExpressionParserContext.getCurrentCharIndex() == 1
        sourceExpressionParserContext.getCurrentChar() == ' ' as char
        sourceExpressionParserContext.getNextChar() == '@' as char
        sourceExpressionParserContext.getNextChar() == ' ' as char

        and:
        sourceExpressionParserContext.skipSpaces()
        sourceExpressionParserContext.skipChars('5' as char)
        sourceExpressionParserContext.skipSpaces()

        then:
        sourceExpressionParserContext.getCurrentChar() == '1' as char
        sourceExpressionParserContext.getNextChar() == '2' as char
        sourceExpressionParserContext.getNextChar() == '3' as char
        sourceExpressionParserContext.getCurrentChar() == '3' as char
        sourceExpressionParserContext.moveToNextChar()

        and:
        def collectedResult = sourceExpressionParserContext.collectTextUntilAnyChars('.' as char, '#' as char)

        then:
        collectedResult.collectedText == 'cvx'
        collectedResult.cutWithText == '.' as char
        sourceExpressionParserContext.getCurrentChar() == 'g' as char

        and:
        def collectedResult2 = sourceExpressionParserContext.collectTextUntilAnyCharsOrTillEnd("." as char)

        then:
        collectedResult2.collectedText == 'gfa)h'
        collectedResult2.cutByEOF
        sourceExpressionParserContext.currentChar == 'h' as char

        then:
        sourceExpressionParserContext.moveToPreviousChar()
        sourceExpressionParserContext.moveToPreviousChar()
        sourceExpressionParserContext.moveToPreviousChar()
        sourceExpressionParserContext.moveToPreviousChar()
        sourceExpressionParserContext.currentChar == 'g' as char

        and:
        def collectedResult3 = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished()
        collectedResult3.collectedText == 'gfa'
        collectedResult3.cutWithText == ')' as char
        !collectedResult3.cutByEOF
        sourceExpressionParserContext.currentCharIs('h' as char)
        sourceExpressionParserContext.isLastCurrentChar()
        sourceExpressionParserContext.moveToNextCharIfExists()
        sourceExpressionParserContext.isLastCurrentChar()
    }

    def "collectTextUntilAnyChars throws parse exception"() {
        given:

        when:
        sourceExpressionParserContext.collectTextUntilAnyChars('#' as char, '%' as char)

        then:
        EntryMappingParseException ex = thrown()
        ex.message == createParseExceptionMessage(26, createMessagePlaceholder("expected.any.of.chars", '#, %')).toString()
    }

    def "cannot find next char"() {
        given:
        sourceExpressionParserContext.collectTextUntilAnyCharsOrTillEnd('#' as char)

        when:
        def wasLastChar = sourceExpressionParserContext.isLastCurrentChar()
        sourceExpressionParserContext.moveToNextChar()

        then:
        wasLastChar
        EntryMappingParseException ex = thrown()
        ex.message == createParseExceptionMessage(26, createMessagePlaceholder("mapper.parser.expected.any.char")).toString()
    }

    private static EntryMappingParseException createExpectedException(int column, MessagePlaceholder messagePlaceholder) {
        new EntryMappingParseException(createParseExceptionMessage(column, messagePlaceholder))
    }

    private static MapperContextEntryError createParseExceptionMessage(int column, MessagePlaceholder messagePlaceholder) {
        MapperContextEntryError.builder()
            .entryIndex(0)
            .columnNumber(column)
            .errorReason(messagePlaceholder.translateMessage())
            .build()
    }

}
