package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NullableCollectionUtils {

    public static <T, R> List<R> nullableMapToList(Collection<T> collection, Function<T, R> mapFunc) {
        return Optional.ofNullable(collection)
            .map(notNullCollection -> mapToList(collection, mapFunc))
            .orElse(List.of());
    }
}
