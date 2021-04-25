package pl.jalokim.crudwizard.core.datetime;

import java.time.ZoneId;
import java.util.Optional;
import pl.jalokim.crudwizard.core.exception.BusinessLogicException;

public class TimeZoneRequestHolder {

    public static final String X_TIMEZONE_NAME_HEADER = "X-Timezone-Name";
    public static final ThreadLocal<ZoneId> ZONE_NAME_REQUEST_HOLDER = new ThreadLocal<>();

    public static ZoneId geRequestedZoneId() {
        return Optional.ofNullable(ZONE_NAME_REQUEST_HOLDER.get())
            .orElseThrow(() -> new BusinessLogicException(String.format("required %s header", X_TIMEZONE_NAME_HEADER)));
    }

    public static void setZoneNameForRequest(ZoneId zoneId) {
        ZONE_NAME_REQUEST_HOLDER.set(zoneId);
    }

    public static boolean hasTimeZoneHeader() {
        return ZONE_NAME_REQUEST_HOLDER.get() != null;
    }

    public static void clear() {
        ZONE_NAME_REQUEST_HOLDER.remove();
    }
}
