package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import static java.util.Optional.ofNullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.InstanceLoader;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;

@Component
public class QueryProviderMapper {

    @Autowired
    private InstanceLoader instanceLoader;

    public DataStorageQueryProvider mapInstance(QueryProviderEntity queryProviderEntity) {
        return ofNullable(queryProviderEntity)
            .map(queryProvider -> (DataStorageQueryProvider) instanceLoader.createInstanceOrGetBean(queryProvider.getClassName()))
            .orElse(null);
    }
}
