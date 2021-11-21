package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@Component
@RequiredArgsConstructor
public class ValidatorInstanceCache {

    private final Map<String, DataValidator<?>> dataValidatorsByKey = new ConcurrentHashMap<>();
    private final InstanceLoader instanceLoader;

    public DataValidator<?> loadInstance(String className) {
        return dataValidatorsByKey
            .computeIfAbsent(className, instanceLoader::createInstanceOrGetBean);
    }
}
