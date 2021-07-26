package pl.jalokim.crudwizard.core.utils;

import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.collection.Elements;

@UtilityClass
public class ElementsUtils {

    public static <T> Elements<T> nullableElements(List<T> list) {
        return Optional.ofNullable(list)
            .map(Elements::elements)
            .orElse(Elements.empty());
    }

}
