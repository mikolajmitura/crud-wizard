package pl.jalokim.crudwizard.genericapp.datastorage;

import java.util.Map;
import org.springframework.context.ApplicationContext;

public interface DataStorageFactory<D extends DataStorage> {

    D createInstance(String name, Map<String, Object> configuration, ApplicationContext applicationContext);
}
