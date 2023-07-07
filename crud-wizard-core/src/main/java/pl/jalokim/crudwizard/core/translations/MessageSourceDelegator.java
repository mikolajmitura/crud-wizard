package pl.jalokim.crudwizard.core.translations;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

@RequiredArgsConstructor
public class MessageSourceDelegator implements MessageSource {

    private final List<MessageSourceProvider> messageSourceProviders;
    private final LocaleService localeService;

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            return getMessage(locale, code,
                messageSource -> messageSource.getMessage(code, args, locale));
        } catch (NoSuchMessageException ex) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessage(locale, code, messageSource -> messageSource.getMessage(code, args, locale));
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        List<String> properties = elements(resolvable.getCodes()).asList();
        NoSuchMessageException lastException = new NoSuchMessageException(properties.toString(), locale);
        for (String property : properties) {
            try {
                return getMessage(locale, property, messageSource -> messageSource.getMessage(resolvable, locale));
            } catch (NoSuchMessageException ex) {
                lastException = ex;
            }
        }
        throw lastException;
    }

    private String getMessage(Locale locale, String property, Function<RefreshableMessageSource, String> getMessageFunction) {
        List<String> allSupportedLocales = elements(localeService.getAllSupportedLocales())
            .map(Locale::toString)
            .map(String::toLowerCase)
            .asList();
        if (!allSupportedLocales.contains(locale.toString().toLowerCase())) {
            throw new IllegalArgumentException("not supported locale: " + locale);
        }

        for (MessageSourceProvider messageSourceProvider: messageSourceProviders) {
            if (messageSourceProvider.isSupportingLanguageAndProperty(locale, property)) {
                return getMessageFunction.apply(messageSourceProvider.getMessageSource());
            }
        }
        throw new NoSuchMessageException(property, locale);
    }
}
