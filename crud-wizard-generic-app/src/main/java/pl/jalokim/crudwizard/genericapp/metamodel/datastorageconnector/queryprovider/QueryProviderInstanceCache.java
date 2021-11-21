package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@Component
@RequiredArgsConstructor
public class QueryProviderInstanceCache {

    private final Map<String, DataStorageQueryProvider> queryProvidersByClassName = new ConcurrentHashMap<>();
    private final InstanceLoader instanceLoader;

    public DataStorageQueryProvider loadQueryProvider(String className) {
        return queryProvidersByClassName.computeIfAbsent(className, instanceLoader::createInstanceOrGetBean);
    }
}
