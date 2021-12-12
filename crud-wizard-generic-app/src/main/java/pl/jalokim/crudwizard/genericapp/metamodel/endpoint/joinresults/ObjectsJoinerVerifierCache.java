package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.ObjectsJoinerVerifier;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@Component
@RequiredArgsConstructor
public class ObjectsJoinerVerifierCache {

    private final Map<String, ObjectsJoinerVerifier<?, ?>> objectJointerVerifierByClassName = new ConcurrentHashMap<>();
    private final InstanceLoader instanceLoader;

    public ObjectsJoinerVerifier<?, ?> loadJoinerVerifier(String className) {
        return objectJointerVerifierByClassName.computeIfAbsent(className, instanceLoader::createInstanceOrGetBean);
    }
}
