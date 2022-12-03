package pl.jalokim.crudwizard.genericapp.service.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DefaultSubClassesForAbstractClassesConfig {

    @SuppressWarnings("PMD.LooseCoupling")
    private static final Map<Class<?>, Class<?>> DEFAULT_CONFIG = Map.of(
        List.class, ArrayList.class,
        Set.class, HashSet.class,
        Map.class, HashMap.class
    );

    public Map<Class<?>, Class<?>> returnConfig() {
        return DEFAULT_CONFIG;
    }
}
