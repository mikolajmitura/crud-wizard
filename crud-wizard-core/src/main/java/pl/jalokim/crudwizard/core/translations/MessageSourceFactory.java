package pl.jalokim.crudwizard.core.translations;

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
}
