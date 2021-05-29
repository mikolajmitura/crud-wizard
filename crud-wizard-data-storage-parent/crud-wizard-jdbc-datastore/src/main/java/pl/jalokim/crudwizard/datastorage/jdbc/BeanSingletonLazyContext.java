package pl.jalokim.crudwizard.datastorage.jdbc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class BeanSingletonLazyContext<T> {

    private final AtomicReference<T> createdInstance = new AtomicReference<>();
    private final Supplier<T> objectSupplier;

    public T getBeanInstance() {
        if (createdInstance.get() == null) {
            createdInstance.set(objectSupplier.get());
        }
        return createdInstance.get();
    }

}
