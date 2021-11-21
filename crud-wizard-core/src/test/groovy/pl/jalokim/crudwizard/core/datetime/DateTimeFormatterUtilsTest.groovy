package pl.jalokim.crudwizard.core.datetime

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings("UnnecessarySetter")
class DateTimeFormatterUtilsTest extends Specification {

    private static TimeZone defaultTimeZoneId

    def setup() {
        defaultTimeZoneId = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    def cleanup() {
        TimeZone.setDefault(defaultTimeZoneId)
    }

    @Unroll
    def "should return: #expectedDateTimeAsText when given is Offset: #timeOffset"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2020, 5, 11, 11, 12, 15)

        when:
        def result = DateTimeFormatterUtils.formatAsDateTimeInZoneOffset(localDateTime, targetZoneId)

        then:
        result == expectedDateTimeAsText

        where:
        targetZoneId               || expectedDateTimeAsText
        ZoneOffset.UTC             || "2020-05-11 11:12"
        ZoneId.of("Europe/Warsaw") || "2020-05-11 13:12"
        ZoneId.of("Etc/GMT+2")     || "2020-05-11 09:12"
    }

    def "should return the same date and time when default zone offset is the same like target"() {
        given:
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Belgrade"))
        ZoneOffset targetZoneOffset = ZoneId.systemDefault().getOffset()
        LocalDateTime localDateTime = LocalDateTime.of(2020, 5, 11, 15, 12, 15)

        when:
        def result = DateTimeFormatterUtils.formatAsDateTimeInZoneOffset(localDateTime, targetZoneOffset)

        then:
        result == "2020-05-11 15:12" || result == "2020-05-11 14:12"
    }

    @Unroll
    def "should return: #expectedDateTimeAsText when given is Offset: #timeOffset and format is: #dateTimeFormat"() {
        given:
        LocalDateTime localDateTime = LocalDateTime.of(2020, fromMonth, 11, 11, 12, 15)

        when:
        def result = DateTimeFormatterUtils.formatAsDateTimeInZoneOffset(localDateTime, targetZoneId, dateTimeFormat)

        then:
        result == expectedDateTimeAsText

        where:
        targetZoneId               | dateTimeFormat        | fromMonth || expectedDateTimeAsText
        ZoneOffset.UTC             | "yyyy-MM-dd HH:mm:ss" | 5         || "2020-05-11 11:12:15"
        ZoneId.of("Europe/Warsaw") | "HH:mm yyyy-MM-dd"    | 5         || "13:12 2020-05-11"
        ZoneId.of("Europe/Warsaw") | "HH:mm yyyy-MM-dd"    | 2         || "12:12 2020-02-11"
        ZoneId.of("Etc/GMT+2")     | "yyyy-MM-dd"          | 5         || "2020-05-11"
    }
}
