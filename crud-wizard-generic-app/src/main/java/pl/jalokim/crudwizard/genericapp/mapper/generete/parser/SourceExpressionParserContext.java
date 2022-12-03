package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.utils.collection.CollectionUtils;

@Data
public class SourceExpressionParserContext {

    private final String currentWholeExpression;
    private final MapperConfigurationParserContext mapperConfigurationParserContext;
    private final MapperConfiguration mapperConfiguration;
    private final ClassMetaModel targetFieldClassMetaModel;

    private int currentCharIndex;
    private final List<ClassMetaModel> castMetaModels = new ArrayList<>();

    @Getter
    @Setter
    public ValueToAssignExpression earlierValueToAssignExpression;

    public SourceExpressionParserContext(String currentWholeExpression,
        MapperConfigurationParserContext mapperConfigurationParserContext,
        MapperConfiguration mapperConfiguration, ClassMetaModel targetFieldClassMetaModel) {

        this.currentWholeExpression = currentWholeExpression.stripTrailing();
        this.mapperConfigurationParserContext = mapperConfigurationParserContext;
        this.mapperConfiguration = mapperConfiguration;
        this.targetFieldClassMetaModel = targetFieldClassMetaModel;
    }

    public CollectedExpressionPartResult collectTextUntilAnyChars(char... untilChars) {
        Set<Character> untilCharsSet = new HashSet<>();
        for (char untilChar : untilChars) {
            untilCharsSet.add(untilChar);
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            char currentChar = getCurrentChar();
            if (untilCharsSet.contains(currentChar)) {
                moveToNextCharIfExists();
                return new CollectedExpressionPartResult(stringBuilder.toString(), currentChar);
            }
            if (isLastCurrentChar()) {
                mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("expected.any.of.chars",
                    elements(untilCharsSet).asConcatText(", ")));
            } else {
                stringBuilder.append(currentChar);
                moveToNextChar();
            }
        }
    }

    @SuppressWarnings("PMD.AvoidArrayLoops")
    public CollectedExpressionPartResult collectTextUntilFieldExpressionIsFinished(char... untilChars) {
        char[] charsWhichMeansExpressionFinish = {'.', ')', ','};
        char[] newChars = new char[charsWhichMeansExpressionFinish.length + untilChars.length];

        System.arraycopy(charsWhichMeansExpressionFinish, 0,
            newChars, 0, charsWhichMeansExpressionFinish.length);

        for (int i = 0; i < untilChars.length; i++) {
            int offsetIndex = charsWhichMeansExpressionFinish.length + i;
            newChars[offsetIndex] = untilChars[i];
        }

        return collectTextUntilAnyCharsOrTillEnd(newChars);
    }

    public CollectedExpressionPartResult collectTextUntilAnyCharsOrTillEnd(char... untilChars) {
        Set<Character> untilCharsSet = new HashSet<>();
        for (char untilChar : untilChars) {
            untilCharsSet.add(untilChar);
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            char currentChar = getCurrentChar();
            if (untilCharsSet.contains(currentChar)) {
                moveToNextCharIfExists();
                return new CollectedExpressionPartResult(stringBuilder.toString(), currentChar);
            } else {
                stringBuilder.append(currentChar);
                if (isLastCurrentChar()) {
                    return new CollectedExpressionPartResult(stringBuilder.toString(), '\n');
                }
                moveToNextChar();
            }
        }
    }

    public void skipChars(char charToSkip) {
        while (true) {
            if (isLastCurrentChar()) {
                break;
            }
            char nextChar = getCurrentChar();
            if (nextChar == charToSkip) {
                moveToNextChar();
            } else {
                break;
            }
        }
    }

    public void skipSpaces() {
        skipChars(' ');
    }

    public char getNextChar() {
        moveToNextChar();
        return getCurrentChar();
    }

    public void moveToNextChar() {
        currentCharIndex++;
        validateCharIndex();
    }

    public void nextCharIs(char expectedChar) {
        char nextChar = getNextChar();
        if (nextChar != expectedChar) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("expected.any.of.chars", expectedChar));
        }
    }

    public void currentCharIs(char expectedChar) {
        char currentChar = getCurrentChar();
        if (currentChar != expectedChar) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("expected.any.of.chars", expectedChar));
        }
    }

    public char getCurrentChar() {
        if (isReadAll()) {
            return '\u0000';
        }
        validateCharIndex();
        return currentWholeExpression.charAt(currentCharIndex);
    }

    public void moveToPreviousChar() {
        currentCharIndex--;
        if (currentCharIndex < 0) {
            throwExceptionWhenInvalidCharIndex();
        }
    }

    public void moveToNextCharIfExists() {
        if (isLastCurrentChar() || isReadAll()) {
            currentCharIndex++;
        } else {
            moveToNextChar();
        }
    }

    private void validateCharIndex() {
        if (isReadAll()) {
            throwExceptionWhenInvalidCharIndex();
        }
    }

    private void throwExceptionWhenInvalidCharIndex() {
        mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("mapper.parser.expected.any.char"));
    }

    public boolean isLastCurrentChar() {
        return currentWholeExpression.length() == 0 && currentCharIndex == 0 || currentWholeExpression.length() - 1 == currentCharIndex;
    }

    public boolean isReadAll() {
        return currentCharIndex >= currentWholeExpression.length();
    }

    public ClassMetaModel getSourceMetaModel() {
        return mapperConfiguration.getSourceMetaModel();
    }

    public ClassMetaModel getTargetMetaModel() {
        return mapperConfiguration.getTargetMetaModel();
    }

    public MapperGenerateConfiguration getMapperGenerateConfiguration() {
        return mapperConfigurationParserContext.getMapperGenerateConfiguration();
    }

    public void addClassMetaModelCast(ClassMetaModel classMetaModelAsCast) {
        castMetaModels.add(classMetaModelAsCast);
    }

    public ClassMetaModel getCurrentClassMetaModelAsCast() {
        return CollectionUtils.getFirstOrNull(castMetaModels);
    }

    public void clearLastCastMetaModel() {
        castMetaModels.remove(CollectionUtils.getLastIndex(castMetaModels));
    }
}
