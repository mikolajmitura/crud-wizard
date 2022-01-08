package pl.jalokim.crudwizard.test.utils.translations;

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY;
import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY;
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.APPLICATION_TRANSLATIONS_PATH;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.CORE_APPLICATION_TRANSLATIONS;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMessageSourceDelegator;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.EMAIL_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.LENGTH_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.MAX_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.MIN_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_BLANK_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_EMPTY_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.PATTERN_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.RANGE_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.SIZE_MESSAGE_PROPERTY;
import static pl.jalokim.crudwizard.test.utils.validation.TestingConstraintValidatorFactory.createTestingValidatorFactory;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.ValidatorFactory;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import pl.jalokim.crudwizard.core.translations.MessageSourceFactory;
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider;
import pl.jalokim.crudwizard.core.translations.SpringAppMessageSource;
import pl.jalokim.crudwizard.core.translations.TestAppMessageSourceHolder;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.WhenFieldIsInStateThenOthersShould;
import pl.jalokim.utils.constants.Constants;

/**
 * Useful for testing without spring context.
 */
public final class AppMessageSourceTestImpl extends SpringAppMessageSource {

    public static final String TEST_APPLICATION_TRANSLATIONS_PATH = "test-application-translations";

    public static AppMessageSourceTestImpl initStaticAppMessageSource() {
        return initStaticAppMessageSource(APPLICATION_TRANSLATIONS_PATH, CORE_APPLICATION_TRANSLATIONS, TEST_APPLICATION_TRANSLATIONS_PATH);
    }

    public static AppMessageSourceTestImpl initStaticAppMessageSource(String... resourcePaths) {
        return initStaticAppMessageSource(true, resourcePaths);
    }

    @SuppressWarnings("PMD.CloseResource")
    public static AppMessageSourceTestImpl initStaticAppMessageSource(boolean setupInStaticHolder, String... resourcePaths) {
        MessageSource messageSourceDelegator = createMessageSourceDelegator(createMessageSources(resourcePaths));
        ValidatorFactory testingValidatorFactory = createTestingValidatorFactory(messageSourceDelegator);
        return new AppMessageSourceTestImpl(setupInStaticHolder, messageSourceDelegator, testingValidatorFactory);
    }

    @SneakyThrows
    private AppMessageSourceTestImpl(boolean setupInStaticHolder, MessageSource messageSource, ValidatorFactory testingValidatorFactory) {
        super(messageSource, testingValidatorFactory);
        if (setupInStaticHolder) {
            TestAppMessageSourceHolder.setAppMessageSource(this);
        }
    }

    public static MessageSourceProvider createMessageSourceProvider(String propertyPath) {
        return MessageSourceFactory.createMessageSourceProvider(propertyPath);
    }

    public static List<MessageSourceProvider> createMessageSources(String... resourcePaths) {
        return elements(resourcePaths)
            .map(AppMessageSourceTestImpl::createMessageSourceProvider)
            .asList();
    }

    public static String notNullMessage() {
        return getAppMessageSource().getMessage(NOT_NULL_MESSAGE_PROPERTY);
    }

    public static String notBlankMessage() {
        return getAppMessageSource().getMessage(NOT_BLANK_MESSAGE_PROPERTY);
    }

    public static String notEmptyMessage() {
        return getAppMessageSource().getMessage(NOT_EMPTY_MESSAGE_PROPERTY);
    }

    public static String invalidEmailMessage() {
        return getAppMessageSource().getMessage(EMAIL_MESSAGE_PROPERTY);
    }

    public static String invalidPatternMessage(String regexp) {
        return getAppMessageSource().getMessage(PATTERN_MESSAGE_PROPERTY, Map.of("regexp", regexp));
    }

    public static String invalidSizeMessage(Integer min, Integer max) {
        int minValue = Optional.ofNullable(min).orElse(0);
        int maxValue = Optional.ofNullable(max).orElse(Integer.MAX_VALUE);
        return getAppMessageSource().getMessage(SIZE_MESSAGE_PROPERTY, Map.of("min", minValue, "max", maxValue));
    }

    public static String invalidLengthMessage(Integer min, Integer max) {
        int minValue = Optional.ofNullable(min).orElse(0);
        int maxValue = Optional.ofNullable(max).orElse(Integer.MAX_VALUE);
        return getAppMessageSource().getMessage(LENGTH_MESSAGE_PROPERTY, Map.of("min", minValue, "max", maxValue));
    }

    public static String invalidRangeMessage(Number min, Number max) {
        return getAppMessageSource().getMessage(RANGE_MESSAGE_PROPERTY, Map.of("min", min, "max", max));
    }

    public static String invalidMinMessage(Number min) {
        return getAppMessageSource().getMessage(MIN_MESSAGE_PROPERTY, Map.of("value", min));
    }

    public static String invalidMaxMessage(Number max) {
        return getAppMessageSource().getMessage(MAX_MESSAGE_PROPERTY, Map.of("value", max));
    }

    public static String messageForValidator(Class<? extends Annotation> validatorAnnotation) {
        return messageForValidator(validatorAnnotation, Map.of());
    }

    public static String messageForValidator(Class<? extends Annotation> validatorAnnotation, String placeholderKey, String placeholderValue) {
        return messageForValidator(validatorAnnotation, Map.of(placeholderKey, placeholderValue));
    }

    public static String messageForValidator(Class<? extends Annotation> validatorAnnotation, Map<String, Object> placeholderArgs) {
        return getAppMessageSource().getMessage(buildMessageForValidator(validatorAnnotation), placeholderArgs);
    }

    public static String entityNotFoundMessage(Object id) {
        return getAppMessageSource().getMessage(EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY, id);
    }

    public static String entityNotFoundMessage(Object id, String entityName) {
        return getAppMessageSource().getMessage(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY, id, entityName);
    }

    public static String resourceAlreadyUpdatedMessage() {
        return getAppMessageSource().getMessage("ResourceChangedException.default.message");
    }

    public static String fieldShouldWhenOtherMessage(ExpectedFieldState should, List<String> fieldValues, String whenField,
        ExpectedFieldState is, List<String> otherFieldValues) {
        return getAppMessageSource().getMessage(
            buildMessageForValidator(FieldShouldWhenOther.class),
            Map.of(
                "should", getAppMessageSource().getMessageByEnumWithPrefix("shouldBe", should),
                "fieldValues", fieldValues.isEmpty() ? Constants.EMPTY : joinValues(fieldValues),
                "whenField", wrapAsExternalPlaceholder(whenField),
                "is", getAppMessageSource().getMessageByEnumWithPrefix("whenIs", is),
                "otherFieldValues", otherFieldValues.isEmpty() ? Constants.EMPTY : joinValues(otherFieldValues)
            )
        );
    }

    public static String whenFieldIsInStateThenOthersShould(String whenField, ExpectedFieldState is, String nestedMessage) {
        return whenFieldIsInStateThenOthersShould(whenField, is, List.of(), nestedMessage);
    }

    public static String whenFieldIsInStateThenOthersShould(String whenField, ExpectedFieldState is, List<String> fieldValues, String nestedMessage) {
        return getAppMessageSource().getMessage(
            buildMessageForValidator(WhenFieldIsInStateThenOthersShould.class),
            Map.of("nestedMessage", nestedMessage,
                "whenField", wrapAsExternalPlaceholder(whenField),
                "is", getAppMessageSource().getMessageByEnumWithPrefix("whenIs", is),
                "fieldValues", fieldValues.isEmpty() ? Constants.EMPTY : joinValues(fieldValues)));
    }

    private static String joinValues(List<String> list) {
        return " " + elements(list).asConcatText(", ");
    }
}
