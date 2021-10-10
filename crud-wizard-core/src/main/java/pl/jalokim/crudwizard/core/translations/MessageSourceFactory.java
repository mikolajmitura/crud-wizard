package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@UtilityClass
public class MessageSourceFactory {

    public static final String APPLICATION_TRANSLATIONS_PATH = "application-translations";
    public static final String CORE_APPLICATION_TRANSLATIONS = "core-application-translations";

    public static MessageSourceProvider createMessageSourceProvider(String messagePath) {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:" + messagePath);
        source.setDefaultEncoding("UTF-8");
        return new MessageSourceProvider(source);
    }

    public static MessageSourceProvider createMessageSourceProvider() {
        return createMessageSourceProvider(APPLICATION_TRANSLATIONS_PATH);
    }

    public static MessageSourceProvider createCommonMessageSourceProvider() {
        // TODO inject here validation message from hibernate validation to support locale etc
        return createMessageSourceProvider(CORE_APPLICATION_TRANSLATIONS);
    }

    public static MessageSource createMessageSourceDelegator(List<MessageSourceProvider> messageSourceProviders) {
        return new MessageSourceDelegator(messageSourceProviders.stream()
            .collect(Collectors.toUnmodifiableList()));
    }
}
