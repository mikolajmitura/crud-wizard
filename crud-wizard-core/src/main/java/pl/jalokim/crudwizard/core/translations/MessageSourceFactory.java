package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;

@UtilityClass
public class MessageSourceFactory {

    public static final String APPLICATION_TRANSLATIONS_PATH = "application-translations";
    public static final String CORE_APPLICATION_TRANSLATIONS = "core-application-translations";
    public static final String HIBERNATE_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

    public static MessageSourceProvider createMessageSourceProvider(String messagePath) {
        ReloadableMessageSource source = new ReloadableMessageSource(messagePath);
        return new MessageSourceProvider(source);
    }

    public static MessageSourceProvider createMessageSourceProvider() {
        return createMessageSourceProvider(APPLICATION_TRANSLATIONS_PATH);
    }

    public static MessageSourceProvider createCommonMessageSourceProvider() {
        return createMessageSourceProvider(CORE_APPLICATION_TRANSLATIONS);
    }

    public static MessageSourceProvider createHibernateMessageSourceProvider() {
        return createMessageSourceProvider(HIBERNATE_VALIDATION_MESSAGES);
    }

    public static MessageSource createMessageSourceDelegator(List<MessageSourceProvider> messageSourceProviders,
        LocaleService localeService) {
        return new MessageSourceDelegator(messageSourceProviders.stream()
            .collect(Collectors.toUnmodifiableList()), localeService);
    }
}
