package pl.jalokim.crudwizard.test.utils;

import static pl.jalokim.crudwizard.core.datetime.DateTimeFormatterUtils.fromTextToOffsetDateTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDateUtils {

    public static String toJsonFormat(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime);
    }

    public static String toJsonFormat(LocalDate localDate) {
        return DateTimeFormatter.ISO_DATE.format(localDate);
    }

    public static boolean assertLocalDate(String expectedDateAsText, LocalDate localDate) {
        if (allAreNull(expectedDateAsText, localDate)) {
            return true;
        }
        return expectedDateAsText.equals(toJsonFormat(localDate));
    }

    public static boolean assertOffsetDateTime(String expectedOffsetDateTimeAsText, OffsetDateTime offsetDateTime) {
        if (allAreNull(expectedOffsetDateTimeAsText, offsetDateTime)) {
            return true;
        }
        return assertOffsetDateTime(fromTextToOffsetDateTime(expectedOffsetDateTimeAsText), offsetDateTime);
    }

    public static boolean assertOffsetDateTime(OffsetDateTime expectedOffsetDateTime, OffsetDateTime offsetDateTime) {
        if (allAreNull(expectedOffsetDateTime, offsetDateTime)) {
            return true;
        }
        return expectedOffsetDateTime.toInstant().equals(offsetDateTime.toInstant());
    }

    public static boolean assertOffsetDateTimeWithoutNanos(String expectedOffsetDateTimeAsText, OffsetDateTime offsetDateTime) {
        if (allAreNull(expectedOffsetDateTimeAsText, offsetDateTime)) {
            return true;
        }
        OffsetDateTime expectedOffsetDateTime = fromTextToOffsetDateTime(expectedOffsetDateTimeAsText);
        return assertOffsetDateTime(expectedOffsetDateTime.truncatedTo(ChronoUnit.SECONDS), offsetDateTime.truncatedTo(ChronoUnit.SECONDS));
    }

    private static boolean allAreNull(Object... objects) {
        return Arrays.stream(objects).allMatch(Objects::isNull);
    }
}

