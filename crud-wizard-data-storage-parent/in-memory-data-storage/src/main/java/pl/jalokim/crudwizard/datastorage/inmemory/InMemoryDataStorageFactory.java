package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.DataStorageFactory;
import pl.jalokim.crudwizard.core.datastorage.query.inmemory.InMemoryDsQueryRunner;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;

@Component
public class InMemoryDataStorageFactory implements DataStorageFactory<InMemoryDataStorage> {

    @Override
    public InMemoryDataStorage createInstance(String name, Map<String, String> configuration, ApplicationContext applicationContext) {
        return new InMemoryDataStorage(name,
            applicationContext.getBean(IdGenerators.class),
            applicationContext.getBean(InMemoryDsQueryRunner.class));
    }
}
