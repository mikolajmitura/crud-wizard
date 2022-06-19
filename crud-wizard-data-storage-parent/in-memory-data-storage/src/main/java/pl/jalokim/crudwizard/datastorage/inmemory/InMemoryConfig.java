package pl.jalokim.crudwizard.datastorage.inmemory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;
import pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory.InMemoryDsQueryRunner;

@Configuration
@ComponentScan("pl.jalokim.crudwizard.datastorage.inmemory")
public class InMemoryConfig {

    @Bean
    public IdGenerators idGenerators() {
        return IdGenerators.INSTANCE;
    }

    @Bean
    public InMemoryDataStorage inMemoryDataStorage(IdGenerators idGenerators, InMemoryDsQueryRunner inMemoryDsQueryRunner) {
        return new InMemoryDataStorage(idGenerators, inMemoryDsQueryRunner);
    }
}
