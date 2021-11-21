package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;

@MetamodelService
public class DataStorageMetaModelService extends BaseService<DataStorageMetaModelEntity, DataStorageMetaModelRepository> {

    private final DataStorageMetaModelMapper dataStorageMetaModelMapper;

    public DataStorageMetaModelService(DataStorageMetaModelRepository repository,
        DataStorageMetaModelMapper dataStorageMetaModelMapper) {
        super(repository);
        this.dataStorageMetaModelMapper = dataStorageMetaModelMapper;
    }

    public List<DataStorageMetaModel> findAllMetaModels() {
        return mapToList(repository.findAll(), dataStorageMetaModelMapper::toFullMetaModel);
    }

    public Long createNewAndGetId(DataStorageMetaModelDto dataStorageMetaModel) {
        return repository.save(dataStorageMetaModelMapper.toEntity(dataStorageMetaModel))
            .getId();
    }

    public boolean exists(DataStorageMetaModelDto dataStorageMetaModel) {
        return repository.existsByNameAndClassName(dataStorageMetaModel.getName(), dataStorageMetaModel.getClassName());
    }
}
