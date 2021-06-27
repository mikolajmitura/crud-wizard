package pl.jalokim.crudwizard.test.utils.translations;

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY;
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.APPLICATION_TRANSLATIONS_PATH;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createCommonMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMainMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMessageSource;
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
import static pl.jalokim.utils.collection.Elements.elements;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.SneakyThrows;
import org.springframework.context.NoSuchMessageException;
import pl.jalokim.crudwizard.core.translations.SpringAppMessageSource;
import pl.jalokim.crudwizard.core.translations.TestAppMessageSourceHolder;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.test.utils.validation.TestingConstraintValidatorFactory;
import pl.jalokim.utils.constants.Constants;

/**
 * Useful for testing without spring context.
 */
public class AppMessageSourceTestImpl extends SpringAppMessageSource {

    public static final String EXPECTED_TEST_TRANSLATIONS = "expected-test-translations";
    public static AppMessageSourceTestImpl EXPECTED_MESSAGES = new AppMessageSourceTestImpl(EXPECTED_TEST_TRANSLATIONS, false);

    private final Properties properties = new Properties();

    public static void initStaticAppMessageSource() {
        new AppMessageSourceTestImpl();
    }

    @SneakyThrows
    public AppMessageSourceTestImpl(String resourcePath) {
        this(resourcePath, true);
    }

    @SneakyThrows
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public AppMessageSourceTestImpl(String resourcePath, boolean setupInStaticHolder) {
        super(createMainMessageSource(createMessageSource(resourcePath), createCommonMessageSource()));
        if (setupInStaticHolder) {
            TestAppMessageSourceHolder.setAppMessageSource(this);
            TestingConstraintValidatorFactory.initStaticValidatorFactoryHolder();
        }
    }

    @SneakyThrows
    @Deprecated
    public AppMessageSourceTestImpl(File pathToPropertiesFile) {
        super(null);
        try (InputStream inputStream = new FileInputStream(pathToPropertiesFile)) {
            properties.load(inputStream);
        }
        TestAppMessageSourceHolder.setAppMessageSource(this);
    }

    public AppMessageSourceTestImpl() {
        this(APPLICATION_TRANSLATIONS_PATH);
    }

    @Override
    public String getMessage(String propertyKey) {
        if (getMessageSource() != null) {
            return super.getMessage(propertyKey);
        }
        return Optional.ofNullable(properties.getProperty(propertyKey)).orElseThrow(() -> new NoSuchMessageException(propertyKey));
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

    public static String invalidSizeMessage(int min, int max) {
        return getAppMessageSource().getMessage(SIZE_MESSAGE_PROPERTY, Map.of("min", min, "max", max));
    }

    public static String invalidLengthMessage(int min, int max) {
        return getAppMessageSource().getMessage(LENGTH_MESSAGE_PROPERTY, Map.of("min", min, "max", max));
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

    public static String entityNotFoundMessage(Long id) {
        return getAppMessageSource().getMessage(EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY, id);
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

    private static String joinValues(List<String> list) {
        return  " " + elements(list).asConcatText(", ");
    }
}
