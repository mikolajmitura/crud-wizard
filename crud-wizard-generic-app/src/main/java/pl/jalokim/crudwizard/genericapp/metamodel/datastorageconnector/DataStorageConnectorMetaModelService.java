package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;

@RequiredArgsConstructor
@MetamodelService
public class DataStorageConnectorMetaModelService {

    private final DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository;
    private final DataStorageConnectorMetaModelMapper dataStorageConnectorMetaModelMapper;
    private final MapperMetaModelService mapperMetaModelService;
    private final ClassMetaModelService classMetaModelService;
    private final DataStorageMetaModelService dataStorageMetaModelService;

    public DataStorageConnectorMetaModelEntity saveNewDataStorageConnector(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity) {

        dataStorageConnectorMetaModelEntity.setDataStorageMetaModel(dataStorageMetaModelService.saveNewOrLoadById(
            dataStorageConnectorMetaModelEntity.getDataStorageMetaModel()));

        dataStorageConnectorMetaModelEntity.setMapperMetaModelForReturn(mapperMetaModelService.saveNewOrLoadById(
            dataStorageConnectorMetaModelEntity.getMapperMetaModelForReturn()));

        dataStorageConnectorMetaModelEntity.setMapperMetaModelForQuery(mapperMetaModelService.saveNewOrLoadById(
            dataStorageConnectorMetaModelEntity.getMapperMetaModelForQuery()));

        dataStorageConnectorMetaModelEntity.setClassMetaModelInDataStorage(classMetaModelService.saveNewOrLoadById(
            dataStorageConnectorMetaModelEntity.getClassMetaModelInDataStorage()));

        return dataStorageConnectorMetaModelRepository.save(dataStorageConnectorMetaModelEntity);
    }

    public List<DataStorageConnectorMetaModel> getAllMetaModels(MetaModelContext metaModelContext, List<Long> dataStorageConnectorsId) {
        return mapToList(dataStorageConnectorMetaModelRepository.findAllById(dataStorageConnectorsId),
            connectorMetaModelEntity -> dataStorageConnectorMetaModelMapper.toFullMetaModel(metaModelContext, connectorMetaModelEntity)
        );
    }
}
