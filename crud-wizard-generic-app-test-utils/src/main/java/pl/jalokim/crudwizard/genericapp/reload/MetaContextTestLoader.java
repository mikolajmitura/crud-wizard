package pl.jalokim.crudwizard.genericapp.reload;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.datetime.TimeProvider;
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances;

@Service
@RequiredArgsConstructor
public class MetaContextTestLoader {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MetaModelContextService metaModelContextService;
    private final DataStorageInstances dataStorageInstances;
    private final List<DataStorage> defaultDataStorages;
    private final TimeProvider timeProvider;

    public void reload() {
        metaModelContextService.getMetaModelContext()
            .getDataStorages()
            .fetchAll()
            .forEach(it -> {
                if (it.getDataStorage() instanceof InMemoryDataStorage) {
                    ((InMemoryDataStorage) it.getDataStorage()).clear();
                }
            });

        dataStorageInstances.getDataStorages().clear();
        dataStorageInstances.getDataStorages().addAll(defaultDataStorages);

        reloadMetaModelsContext();
    }

    protected void reloadMetaModelsContext() {
        applicationEventPublisher.publishEvent(new MetaModelContextRefreshEvent("reload for test",
            timeProvider.getCurrentOffsetDateTime()));
    }
}
