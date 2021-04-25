package pl.jalokim.crudwizard.test.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import spock.lang.Specification
import spock.lang.Unroll

class TestDateUtilsTest extends Specification {

    @Unroll
    def "should return: #expectedResult for assertion of local dates: #firstValue and #secondValue"() {
        when:
        def result = TestDateUtils.assertLocalDate(firstValue, secondValue)

        then:
        result == expectedResult

        where:
        firstValue   | secondValue               || expectedResult
        "2020-04-12" | LocalDate.of(2020, 4, 12) || true
        "2020-04-11" | LocalDate.of(2020, 4, 13) || false
        null         | null                      || true
    }

    @Unroll
    def "should return: #expectedResult for assertion of offset dateTimes: #firstValue and #secondValue"() {
        when:
        def result = TestDateUtils.assertOffsetDateTime(firstValue, secondValue)

        then:
        result == expectedResult

        where:
        firstValue                                    | secondValue                    || expectedResult
        createOffsetDateTime(13, 15, 1)               | createOffsetDateTime(12, 15, 0) | true
        createOffsetDateTime(13, 15, 0)               | createOffsetDateTime(12, 15, 0) | false
        TestDateUtils.toJsonFormat(createOffsetDateTime(13, 15, 1)) | createOffsetDateTime(12, 15, 0) | true
        TestDateUtils.toJsonFormat(createOffsetDateTime(12, 15, 1)) | createOffsetDateTime(12, 15, 0) | false
        "2020-04-13T14:15:14+02:00"                   | createOffsetDateTime(12, 15, 0) | true
    }

    @Unroll
    def "should return: #expectedResult for assertion of offset dateTimes without nanos: #firstValue and #secondValue"() {
        when:
        def result = TestDateUtils.assertOffsetDateTimeWithoutNanos(firstValue, secondValue)

        then:
        result == expectedResult

        where:
        firstValue                                         | secondValue                         || expectedResult
        TestDateUtils.toJsonFormat(createOffsetDateTime(13, 15, 1, 500)) | createOffsetDateTime(12, 15, 0, 400) | true
    }

    def "assert that both null values are equals"() {
        when:
        def bothAreEquals1 = TestDateUtils.assertOffsetDateTime((String) null, null)
        def bothAreEquals2 = TestDateUtils.assertOffsetDateTime((OffsetDateTime) null, null)

        then:
        bothAreEquals1
        bothAreEquals2
    }

    static OffsetDateTime createOffsetDateTime(int hour, int minute, int timeOffset, int nanos = 0) {
        OffsetDateTime.of(
            LocalDate.of(2020, 4, 13),
            LocalTime.of(hour, minute, 14, nanos),
            ZoneOffset.ofHours(timeOffset)
        )
    }
}
