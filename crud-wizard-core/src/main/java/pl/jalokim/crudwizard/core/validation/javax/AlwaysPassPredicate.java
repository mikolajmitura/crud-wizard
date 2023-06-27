package pl.jalokim.crudwizard.core.validation.javax;

import java.util.function.Predicate;

public class AlwaysPassPredicate implements Predicate<Object> {

    @Override
    public boolean test(Object o) {
        return true;
    }
}
