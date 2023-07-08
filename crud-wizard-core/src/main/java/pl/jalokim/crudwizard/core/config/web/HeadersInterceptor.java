package pl.jalokim.crudwizard.core.config.web;

import static pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder.X_TIMEZONE_NAME_HEADER;
import static pl.jalokim.crudwizard.core.translations.LocaleHolder.X_LOCALE_NAME_HEADER;

import java.time.ZoneId;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder;
import pl.jalokim.crudwizard.core.translations.LocaleHolder;
import pl.jalokim.crudwizard.core.translations.LocaleUtils;

@Component
public class HeadersInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (TimeZoneRequestHolder.hasTimeZoneHeader()) {
            TimeZoneRequestHolder.clear();
        }
        String timeZoneHeader = request.getHeader(X_TIMEZONE_NAME_HEADER);
        if (timeZoneHeader != null) {
            TimeZoneRequestHolder.setZoneNameForRequest(ZoneId.of(timeZoneHeader));
        }

        if (LocaleHolder.hasSetLocale()) {
            LocaleHolder.clear();
        }

        String localeAsText = request.getHeader(X_LOCALE_NAME_HEADER);
        if (localeAsText != null) {
            try {
                Locale locale = LocaleUtils.createLocale(localeAsText);
                LocaleHolder.setLocale(locale);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(ex.getMessage() + " from header: " + X_LOCALE_NAME_HEADER, ex);
            }
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex) {
        TimeZoneRequestHolder.clear();
        LocaleHolder.clear();
    }
}
