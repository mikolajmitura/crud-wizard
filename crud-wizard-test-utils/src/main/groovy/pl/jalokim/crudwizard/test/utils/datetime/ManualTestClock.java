package pl.jalokim.crudwizard.test.utils.datetime;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class ManualTestClock extends Clock implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Clock DEFAULT_CLOCK = Clock.systemDefaultZone();

    private final ZoneId zone;
    private Instant fixedInstant;

    private ManualTestClock() {
        fixedInstant = null;
        this.zone = ZoneId.systemDefault();
    }

    private ManualTestClock(Instant fixedInstant, ZoneId zone) {
        this.fixedInstant = fixedInstant;
        this.zone = zone;
    }

    public static ManualTestClock defaultWithSystemZone() {
        return new ManualTestClock();
    }

    @Override
    public ZoneId getZone() {
        return getZoneInternal();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        if (zone.equals(this.getZoneInternal())) {
            return this;
        }
        if (fixedClockSet()) {
            return new ManualTestClock(this.fixedInstant, zone);
        }
        return DEFAULT_CLOCK.withZone(zone);
    }

    @Override
    public long millis() {
        return getInstantInternal().toEpochMilli();
    }

    @Override
    public Instant instant() {
        return getInstantInternal();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ManualTestClock) {
            ManualTestClock other = (ManualTestClock) obj;
            return getInstantInternal().equals(other.getInstantInternal()) && getZoneInternal().equals(other.getZoneInternal());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getInstantInternal().hashCode() ^ getZoneInternal().hashCode();
    }

    @Override
    public String toString() {
        return "ManualClock(" + (fixedClockSet() ? "FixedClock" : "DefaultSystemClock") + ")[" + getInstantInternal() + "," + getZoneInternal() + "]";
    }

    public void setFixedInstant(Instant fixedInstant) {
        this.fixedInstant = fixedInstant;
    }

    public void reset() {
        fixedInstant = null;
    }

    private ZoneId getZoneInternal() {
        if (fixedClockSet()) {
            return zone;
        }
        return DEFAULT_CLOCK.getZone();
    }

    private boolean fixedClockSet() {
        return fixedInstant != null;
    }

    private Instant getInstantInternal() {
        if (fixedClockSet()) {
            return fixedInstant;
        }
        return DEFAULT_CLOCK.instant();
    }
}
