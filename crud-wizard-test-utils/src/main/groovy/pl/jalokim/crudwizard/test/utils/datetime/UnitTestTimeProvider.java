package pl.jalokim.crudwizard.test.utils.datetime;

import java.time.LocalDate;
import pl.jalokim.crudwizard.core.datetime.CurrentTimeProvider;

public class UnitTestTimeProvider extends CurrentTimeProvider {

    public static final ManualTestClock TEST_CLOCK = ManualTestClock.defaultWithSystemZone();

    public UnitTestTimeProvider() {
        super(TEST_CLOCK);
    }

    public void fixedDate(LocalDate date) {
        TEST_CLOCK.setInstant(date.atStartOfDay().atZone(TEST_CLOCK.getZone()).toInstant());
    }

    public void resetFixedDate() {
        TEST_CLOCK.reset();
    }
}
