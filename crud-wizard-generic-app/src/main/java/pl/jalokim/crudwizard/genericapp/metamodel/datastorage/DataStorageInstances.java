package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.DataStorageFactory;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;

@Component
public class DataStorageInstances {

    private final List<DataStorage> dataStorages;
    private final Map<Class<?>, DataStorageFactory<?>> factoryByType;
    private final ApplicationContext applicationContext;

    public DataStorageInstances(List<DataStorage> dataStorages, List<DataStorageFactory<?>> dataStorageFactories, ApplicationContext applicationContext) {
        this.dataStorages = new CopyOnWriteArrayList<>(dataStorages);
        this.factoryByType = elements(dataStorageFactories)
            .asMap(entry -> getTypeMetadataFromClass(entry.getClass())
                .getTypeMetaDataForParentClass(DataStorageFactory.class)
                .getGenericTypes().get(0).getRawType());
        this.applicationContext = applicationContext;
    }

    public DataStorage findDataStorageOrCreate(DataStorageMetaModelEntity dataStorageMetaModelEntity) {
        return elements(dataStorages)
            .filter(dataStorage -> dataStorage.getClassName().equals(dataStorageMetaModelEntity.getClassName())
                && dataStorage.getName().equals(dataStorageMetaModelEntity.getName()))
            .findFirst()
            .orElseGet(() -> {
                List<AdditionalPropertyEntity> additionalProperties = dataStorageMetaModelEntity.getAdditionalProperties();
                Map<String, String> configuration = elements(additionalProperties)
                    .asMap(AdditionalPropertyEntity::getName, AdditionalPropertyEntity::getRawJson);

                Class<?> realClassOdDs = ClassUtils.loadRealClass(dataStorageMetaModelEntity.getClassName());
                DataStorage dataStorage = factoryByType.get(realClassOdDs)
                    .createInstance(dataStorageMetaModelEntity.getName(), configuration, applicationContext);

                dataStorages.add(dataStorage);

                return dataStorage;
            });
    }
}
