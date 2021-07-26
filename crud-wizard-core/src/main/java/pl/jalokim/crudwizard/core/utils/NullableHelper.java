package pl.jalokim.crudwizard.core.utils;

import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NullableHelper {

    @SuppressWarnings("PMD.AvoidCatchingNPE")
    public static <T> T helpWithNulls(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException ex) {
            return null;
        }
    }
}
