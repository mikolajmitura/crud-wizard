package pl.jalokim.crudwizard.datastorage.inmemory;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorageFactory;
import pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory.InMemoryDsQueryRunner;

@Component
public class InMemoryDataStorageFactory implements DataStorageFactory<InMemoryDataStorage> {

    @Override
    public InMemoryDataStorage createInstance(String name, Map<String, Object> configuration, ApplicationContext applicationContext) {
        return new InMemoryDataStorage(name,
            applicationContext.getBean(IdGenerators.class),
            applicationContext.getBean(InMemoryDsQueryRunner.class));
    }
}
