package pl.jalokim.crudwizard.test.utils.datetime;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class ManualTestClock extends Clock implements Serializable {

    private static final Clock defaultClock = Clock.systemDefaultZone();

    private Instant instant;
    private ZoneId zone;

    private ManualTestClock() {
        instant = null;
        this.zone = ZoneId.systemDefault();
    }

    private ManualTestClock(Instant fixedInstant, ZoneId zone) {
        this.instant = fixedInstant;
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
            return new ManualTestClock(this.instant, zone);
        }
        return defaultClock.withZone(zone);
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

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public void reset() {
        instant = null;
    }

    private ZoneId getZoneInternal() {
        if (fixedClockSet()) {
            return zone;
        }
        return defaultClock.getZone();
    }

    private boolean fixedClockSet() {
        return instant != null;
    }

    private Instant getInstantInternal() {
        if (fixedClockSet()) {
            return instant;
        }
        return defaultClock.instant();
    }
}
