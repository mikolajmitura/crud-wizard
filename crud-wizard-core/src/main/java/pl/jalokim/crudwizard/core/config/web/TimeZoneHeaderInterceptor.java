package pl.jalokim.crudwizard.core.config.web;

import static pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder.X_TIMEZONE_NAME_HEADER;

import java.time.ZoneId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import pl.jalokim.crudwizard.core.datetime.TimeZoneRequestHolder;

@Component
public class TimeZoneHeaderInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (TimeZoneRequestHolder.hasTimeZoneHeader()) {
            TimeZoneRequestHolder.clear();
        }
        String timeZoneHeader = request.getHeader(X_TIMEZONE_NAME_HEADER);
        if (timeZoneHeader != null) {
            TimeZoneRequestHolder.setZoneNameForRequest(ZoneId.of(timeZoneHeader));
        }
        return super.preHandle(request, response, handler);
    }
}
