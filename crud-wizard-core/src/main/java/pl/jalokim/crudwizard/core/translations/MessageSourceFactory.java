package pl.jalokim.crudwizard.core.translations;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class MessageSourceFactory {

    public static final String APPLICATION_TRANSLATIONS_PATH = "application-translations";

    public static MessageSource createMessageSource(String messagePath) {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:" + messagePath);
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    public static MessageSource createMessageSource() {
        return createMessageSource(APPLICATION_TRANSLATIONS_PATH);
    }

    public static MessageSource createCommonMessageSource() {
        // TODO inject here validation message from hibernate validation
        return createMessageSource("core-application-translations");
    }

    public static MessageSource createMainMessageSource(MessageSource appMessageSource, MessageSource commonMessageSource) {
        return new MessageSourceDelegator(Arrays.asList(appMessageSource, commonMessageSource).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList()));
    }
}
