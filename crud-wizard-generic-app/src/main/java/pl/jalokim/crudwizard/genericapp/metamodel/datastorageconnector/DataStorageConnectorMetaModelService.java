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

    public Long saveNewDataStorageConnector(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity) {
        MapperMetaModelEntity mapperMetaModel = dataStorageConnectorMetaModelEntity.getMapperMetaModel();
        if (mapperMetaModel != null && mapperMetaModel.getId() == null) {
            mapperMetaModel.setId(mapperMetaModelEntityRepository.persist(mapperMetaModel).getId());
        }
        ClassMetaModelEntity classMetaModelInDataStorage = dataStorageConnectorMetaModelEntity.getClassMetaModelInDataStorage();
        if (classMetaModelInDataStorage != null && classMetaModelInDataStorage.getId() == null) {
            classMetaModelInDataStorage.setId(classMetaModelService.saveClassModel(classMetaModelInDataStorage).getId());
        }
        Long dataStorageConnectorId = dataStorageConnectorMetaModelRepository.persist(dataStorageConnectorMetaModelEntity).getId();
        dataStorageConnectorMetaModelEntity.setId(dataStorageConnectorId);
        return dataStorageConnectorId;
    }

    public List<DataStorageConnectorMetaModel> getAllMetaModels(MetaModelContext metaModelContext, List<Long> dataStorageConnectorsId) {
        return mapToList(dataStorageConnectorMetaModelRepository.findAllById(dataStorageConnectorsId),
            connectorMetaModelEntity -> dataStorageConnectorMetaModelMapper.toFullMetaModel(metaModelContext, connectorMetaModelEntity)
        );
    }
}
