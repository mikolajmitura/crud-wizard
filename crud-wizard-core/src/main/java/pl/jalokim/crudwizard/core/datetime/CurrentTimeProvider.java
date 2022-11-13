package pl.jalokim.crudwizard.core.datetime;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentTimeProvider implements TimeProvider {

    private final Clock clock;

    @PostConstruct
    public void postInit() {
        TimeProviderHolder.setTimeProvider(this);
    }

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now(clock);
    }

    @Override
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(Clock.tick(clock, ChronoUnit.MILLIS.getDuration()));
    }

    @Override
    public YearMonth getCurrentYearMonth() {
        return YearMonth.now(clock);
    }

    @Override
    public Long getCurrentTimestamp() {
        return clock.instant().toEpochMilli();
    }
}
