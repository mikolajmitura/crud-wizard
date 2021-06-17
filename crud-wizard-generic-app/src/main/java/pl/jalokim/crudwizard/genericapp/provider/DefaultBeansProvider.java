package pl.jalokim.crudwizard.genericapp.provider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.genericapp.config.DefaultBeansConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@Component
@RequiredArgsConstructor
public class DefaultBeansProvider implements ApplicationRunner {

    public final DefaultBeansConfiguration defaultBeansConfiguration;
    public final List<DataStorage> dataStorages;
    public final DataStorageMetaModelService dataStorageMetaModelService;

    private final Map<String, Map<String, DataStorageMetaModel>> dataStorageMetaModelByInfo = new ConcurrentHashMap<>();

    @Override
    public void run(ApplicationArguments args) {

        for (DataStorage dataStorage : dataStorages) {
            DataStorageMetaModel dataStorageMetaModelFromDataStorage = DataStorageMetaModel.builder()
                .name(dataStorage.getName())
                .className(dataStorage.getClassName())
                .build();
            if (!dataStorageMetaModelService.exists(dataStorageMetaModelFromDataStorage)) {
                dataStorageMetaModelService.createNewAndGetId(dataStorageMetaModelFromDataStorage);
            }
        }

        for (DataStorageMetaModel dataStorageMetaModel : dataStorageMetaModelService.findAll()) {
            Objects.requireNonNull(dataStorageMetaModel.getName(), "DataStorageMetaModel name should be not null for: " + dataStorageMetaModel);
            Map<String, DataStorageMetaModel> stringDataStorageMetaModelsByClassName = dataStorageMetaModelByInfo
                .computeIfAbsent(dataStorageMetaModel.getName(), (key) -> new ConcurrentHashMap<>());
            if (stringDataStorageMetaModelsByClassName.get(dataStorageMetaModel.getClassName()) == null) {
                stringDataStorageMetaModelsByClassName.put(dataStorageMetaModel.getClassName(), dataStorageMetaModel);
            } else {
                throw new IllegalArgumentException(String.format(
                    "Found not unique name: %s and class name: %s for DataStorageMetaModel",
                    dataStorageMetaModel.getName(), dataStorageMetaModel.getClassName()));
            }
        }
    }

    public DataStorage getDefaultDataStorage() {
        if (dataStorages.size() == 1) {
            return dataStorages.get(0);
        }

        // TODO #Next 1 test for that by name
        // TODO #Next 1 test for that by name and type
        if (defaultBeansConfiguration.getNameOfDataSource() != null) {
            List<DataStorage> foundByName = Elements.elements(dataStorages)
                .filter(dataStorage -> dataStorage.getName().equals(defaultBeansConfiguration.getNameOfDataSource()))
                .asList();
            if (foundByName.size() == 1) {
                return foundByName.get(0);
            }
            if (defaultBeansConfiguration.getClassOfDataSource() != null) {
                foundByName = Elements.elements(foundByName)
                    .filter(dataStorage -> dataStorage.getName().equals(defaultBeansConfiguration.getClassOfDataSource()))
                    .asList();
                if (foundByName.size() == 1) {
                    return foundByName.get(0);
                }
            }
        }
        // TODO when cannot find by name and type then check that is some is registered as primary DataStorage in spring context
        // TODO #NEXT 2 and test for it
        String dataStoragesInfo = StringUtils.concatElementsAsLines(dataStorages, DataStorage::infoDataStorage);
        throw new IllegalArgumentException(String.format(
            "Found instances of DataStorage: %s %n"
                + "cannot find unique by name via config:%n"
                + "crud.wizard.defaults.nameOfDataSource=%s%n"
                + "or crud.wizard.defaults.classOfDataSource=%s%n",
            dataStoragesInfo,
            defaultBeansConfiguration.getNameOfDataSource(),
            defaultBeansConfiguration.getClassOfDataSource()));
    }

    public DataStorageMetaModel getDefaultDataStorageMetaModel() {
        DataStorage defaultDataStorage = getDefaultDataStorage();
        return dataStorageMetaModelByInfo.get(defaultDataStorage.getName())
            .get(defaultDataStorage.getClassName());
    }
}
