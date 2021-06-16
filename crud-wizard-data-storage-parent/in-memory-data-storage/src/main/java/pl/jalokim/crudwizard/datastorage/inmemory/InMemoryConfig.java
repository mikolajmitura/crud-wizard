package pl.jalokim.crudwizard.datastorage.inmemory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.jalokim.crudwizard.datastorage.inmemory.generator.IdGenerators;

@Configuration
public class InMemoryConfig {

    @Bean
    public IdGenerators idGenerators() {
        return IdGenerators.INSTANCE;
    }

    @Bean
    public InMemoryDataStorage inMemoryDataStorage(IdGenerators idGenerators) {
        return new InMemoryDataStorage(idGenerators);
    }
}
