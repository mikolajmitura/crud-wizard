package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import static java.util.Optional.ofNullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Component
public class QueryProviderMapper {

    @Autowired
    private QueryProviderInstanceCache queryProviderInstanceCache;

    public DataStorageQueryProvider mapInstance(MetaModelContext metaModelContext, QueryProviderEntity queryProviderEntity) {
        return ofNullable(queryProviderEntity)
            .map(queryProvider -> queryProviderInstanceCache.loadQueryProvider(queryProvider.getClassName()))
            .orElseGet(metaModelContext::getDefaultDataStorageQueryProvider);
    }
}
