package pl.jalokim.crudwizard.core.datetime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;

public interface TimeProvider {

    OffsetDateTime getCurrentOffsetDateTime();

    LocalDate getCurrentDate();

    YearMonth getCurrentYearMonth();

    default Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
