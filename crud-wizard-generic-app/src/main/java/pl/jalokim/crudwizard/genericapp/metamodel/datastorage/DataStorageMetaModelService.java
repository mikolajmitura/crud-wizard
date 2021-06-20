package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@AllArgsConstructor
public class DataStorageMetaModelService {

    private final DataStorageMetaModelMapper dataStorageMetaModelMapper;
    private final DataStorageMetaModelRepository dataStorageMetaModelRepository;

    public List<DataStorageMetaModel> findAllMetaModels() {
        return mapToList(dataStorageMetaModelRepository.findAll(), dataStorageMetaModelMapper::toFullMetaModel);
    }

    public Long createNewAndGetId(DataStorageMetaModelDto dataStorageMetaModel) {
        return dataStorageMetaModelRepository.persist(dataStorageMetaModelMapper.toEntity(dataStorageMetaModel))
            .getId();
    }

    public boolean exists(DataStorageMetaModelDto dataStorageMetaModel) {
        return dataStorageMetaModelRepository.existsByNameAndClassName(dataStorageMetaModel.getName(), dataStorageMetaModel.getClassName());
    }
}
