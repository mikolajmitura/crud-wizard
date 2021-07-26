package pl.jalokim.crudwizard.core.datetime;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeFormatterUtils {

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public static String formatAsDateTimeInZoneOffset(LocalDateTime localDateTime, ZoneId targetZoneId) {
        return formatAsDateTimeInZoneOffset(localDateTime, targetZoneId, DEFAULT_DATE_TIME_FORMAT);
    }

    public static String formatAsDateTimeInZoneOffset(LocalDateTime localDateTime, ZoneId targetZoneId, String dateTimeFormat) {
        ZonedDateTime zonedDateTimeInDefaultZone = localDateTime.atZone(ZoneId.systemDefault());
        OffsetDateTime offsetDateTimeInGivenOffset = zonedDateTimeInDefaultZone.withZoneSameInstant(targetZoneId).toOffsetDateTime();
        return offsetDateTimeInGivenOffset.format(DateTimeFormatter.ofPattern(dateTimeFormat));
    }

    public static OffsetDateTime fromTextToOffsetDateTime(String offsetDateTimeAsText) {
        return OffsetDateTime.parse(offsetDateTimeAsText, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
