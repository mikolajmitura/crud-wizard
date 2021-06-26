package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;

@RequiredArgsConstructor
@MetamodelService
public class DataStorageConnectorMetaModelService {

    private final DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository;
    private final DataStorageConnectorMetaModelMapper dataStorageConnectorMetaModelMapper;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final ClassMetaModelService classMetaModelService;

    public DataStorageConnectorMetaModelEntity saveNewDataStorageConnector(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity) {
        MapperMetaModelEntity mapperMetaModel = dataStorageConnectorMetaModelEntity.getMapperMetaModel();
        if (mapperMetaModel != null && mapperMetaModel.getId() == null) {
            dataStorageConnectorMetaModelEntity.setMapperMetaModel(mapperMetaModelEntityRepository.persist(mapperMetaModel));
        }
        ClassMetaModelEntity classMetaModelInDataStorage = dataStorageConnectorMetaModelEntity.getClassMetaModelInDataStorage();
        if (classMetaModelInDataStorage != null && classMetaModelInDataStorage.getId() == null) {
            dataStorageConnectorMetaModelEntity.setClassMetaModelInDataStorage(classMetaModelService.saveClassModel(classMetaModelInDataStorage));
        }
        return dataStorageConnectorMetaModelRepository.persist(dataStorageConnectorMetaModelEntity);
    }

    public List<DataStorageConnectorMetaModel> getAllMetaModels(MetaModelContext metaModelContext, List<Long> dataStorageConnectorsId) {
        return mapToList(dataStorageConnectorMetaModelRepository.findAllById(dataStorageConnectorsId),
            connectorMetaModelEntity -> dataStorageConnectorMetaModelMapper.toFullMetaModel(metaModelContext, connectorMetaModelEntity)
        );
    }
}
