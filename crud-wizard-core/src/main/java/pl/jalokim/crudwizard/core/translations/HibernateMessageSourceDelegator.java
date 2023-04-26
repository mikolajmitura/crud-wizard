package pl.jalokim.crudwizard.core.translations;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Added due to fact that when is created interpolation by hibernate then it using default locale from system.
 * This delegates to locale from LocaleHolder
 */
@RequiredArgsConstructor
public class HibernateMessageSourceDelegator implements MessageSource {

    private final MessageSource messageSource;

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleHolder.getLocale());
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(code, args, LocaleHolder.getLocale());
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(resolvable, LocaleHolder.getLocale());
    }
}
