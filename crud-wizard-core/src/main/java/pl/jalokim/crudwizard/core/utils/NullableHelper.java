package pl.jalokim.crudwizard.core.utils;

import java.util.function.Supplier;

public class NullableHelper {

    public static <T> T helpWithNulls(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException ex) {
            // nop
            return null;
        }
    }
}
