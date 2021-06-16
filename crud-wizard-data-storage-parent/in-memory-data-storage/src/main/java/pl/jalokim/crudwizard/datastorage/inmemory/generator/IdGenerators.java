package pl.jalokim.crudwizard.datastorage.inmemory.generator;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import pl.jalokim.crudwizard.core.exception.TechnicalException;

public class IdGenerators {

    public static final IdGenerators INSTANCE = new IdGenerators();

    private static final Map<Class<?>, IdGenerator<?>> ID_GENERATORS_BY_CLASS = Map.of(
        String.class, () -> UUID.randomUUID().toString(),
        Long.class, new IdGenerator<Long>() {

            private final AtomicLong currentValue = new AtomicLong();

            @Override
            public Long getNext() {
                return currentValue.getAndIncrement();
            }
        },
        Integer.class, new IdGenerator<Integer>() {

            private final AtomicInteger currentValue = new AtomicInteger();

            @Override
            public Integer getNext() {
                return currentValue.getAndIncrement();
            }
        }
    );

    public Map<Class<?>, IdGenerator<?>> getIdGenerators() {
        return ID_GENERATORS_BY_CLASS;
    }

    public Object getNextFor(Class<?> forType) {
        return Optional.ofNullable(getIdGenerators().get(forType))
            .orElseThrow(() -> new TechnicalException("Cannot find id generator for class type: " + forType.getCanonicalName()))
            .getNext();
    }
}
