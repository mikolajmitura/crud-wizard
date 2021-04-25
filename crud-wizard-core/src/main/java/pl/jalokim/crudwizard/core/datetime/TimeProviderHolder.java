package pl.jalokim.crudwizard.core.datetime;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeProviderHolder {

    private static final AtomicReference<TimeProvider> TIME_PROVIDER_INSTANCE = new AtomicReference<>();

    public static TimeProvider getTimeProvider() {
        return Optional.ofNullable(TIME_PROVIDER_INSTANCE.get())
            .orElseThrow(() -> new NoSuchElementException("TIME_PROVIDER is not set"));
    }

    static void setTimeProvider(TimeProvider timeProvider) {
        TIME_PROVIDER_INSTANCE.set(timeProvider);
    }

}
