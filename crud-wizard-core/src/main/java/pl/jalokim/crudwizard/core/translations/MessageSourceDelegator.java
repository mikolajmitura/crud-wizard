package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import pl.jalokim.utils.collection.Elements;

public class MessageSourceDelegator implements MessageSource {

    private final List<MessageSource> messageSources;

    public MessageSourceDelegator(List<MessageSourceProvider> messageSourceProviders) {
        this.messageSources = Elements.elements(messageSourceProviders)
            .map(MessageSourceProvider::getMessageSource)
            .asList();
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            return getMessage(messageSource -> messageSource.getMessage(code, args, locale));
        } catch (NoSuchMessageException ex) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessage(messageSource -> messageSource.getMessage(code, args, locale));
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return getMessage(messageSource -> messageSource.getMessage(resolvable, locale));
    }

    private String getMessage(Function<MessageSource, String> getMessageFunction) {
        NoSuchMessageException lastException = null;
        for (MessageSource messageSource : messageSources) {
            try {
                return getMessageFunction.apply(messageSource);
            } catch (NoSuchMessageException ex) {
                lastException = ex;
            }
        }
        throw lastException;
    }
}
