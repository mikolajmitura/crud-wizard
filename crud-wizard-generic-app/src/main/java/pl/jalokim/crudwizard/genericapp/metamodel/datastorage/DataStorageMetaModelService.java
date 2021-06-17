package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import java.util.List;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.utils.collection.Elements;

@MetamodelService
@AllArgsConstructor
public class DataStorageMetaModelService {

    private final DataStorageMetaModelMapper dataStorageMetaModelMapper;
    private final DataStorageMetaModelRepository dataStorageMetaModelRepository;

    public List<DataStorageMetaModel> findAll() {
        return dataStorageMetaModelMapper.toDtoList(dataStorageMetaModelRepository.findAll());
    }

    public Long createNewAndGetId(DataStorageMetaModel dataStorageMetaModel) {
        return dataStorageMetaModelRepository.persist(dataStorageMetaModelMapper.toEntity(dataStorageMetaModel))
            .getId();
    }

    public boolean exists(DataStorageMetaModel dataStorageMetaModel) {
        return Elements.elements(findAll())
            .filter(dataStorageMetaModelElement -> dataStorageMetaModelElement.getName().equals(dataStorageMetaModel.getName()))
            .anyMatch(dataStorageMetaModelElement -> dataStorageMetaModelElement.getClassName().equals(dataStorageMetaModel.getClassName()));
    }
}
