package pl.jalokim.crudwizard.core.datastorage;

import java.util.Map;
import org.springframework.context.ApplicationContext;

public interface DataStorageFactory<D extends DataStorage> {

    D createInstance(String name, Map<String, String> configuration, ApplicationContext applicationContext);
}
