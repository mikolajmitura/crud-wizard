package pl.jalokim.crudwizard.test.utils.translations;

import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.APPLICATION_TRANSLATIONS_PATH;

import java.util.Optional;
import java.util.Properties;
import lombok.SneakyThrows;
import org.springframework.context.NoSuchMessageException;
import pl.jalokim.crudwizard.core.translations.MessageSourceFactory;
import pl.jalokim.crudwizard.core.translations.SpringAppMessageSource;
import pl.jalokim.crudwizard.core.translations.TestAppMessageSourceHolder;

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
    public AppMessageSourceTestImpl(String resourcePath, boolean setupInStaticHolder) {
        super(MessageSourceFactory.createMessageSource(resourcePath));
        if (setupInStaticHolder) {
            TestAppMessageSourceHolder.setAppMessageSource(this);
        }
    }

    @SneakyThrows
    public AppMessageSourceTestImpl(String resourcePath) {
        this(resourcePath, true);
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
}
