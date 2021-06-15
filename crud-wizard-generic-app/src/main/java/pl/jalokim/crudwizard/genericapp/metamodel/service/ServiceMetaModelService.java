package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;

@RequiredArgsConstructor
@MetamodelService
public class ServiceMetaModelService {

    private final ServiceMetaModelRepository serviceMetaModelRepository;
    private final ServiceMetaModelMapper serviceMetaModelMapper;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final ClassMetaModelService classMetaModelService;

    public ServiceMetaModelEntity saveServiceMetaModel(ServiceMetaModelEntity serviceMetaModel) {
        elements(serviceMetaModel.getDataStorageConnectors())
            .forEach(connectorEntry -> {
                MapperMetaModelEntity mapperMetaModel = connectorEntry.getMapperMetaModel();
                if (mapperMetaModel != null && mapperMetaModel.getId() == null) {
                    mapperMetaModel.setId(mapperMetaModelEntityRepository.persist(mapperMetaModel).getId());
                }
                ClassMetaModelEntity classMetaModelInDataStorage = connectorEntry.getClassMetaModelInDataStorage();
                if (classMetaModelInDataStorage != null && classMetaModelInDataStorage.getId() == null) {
                    classMetaModelInDataStorage.setId(classMetaModelService.saveClassModel(classMetaModelInDataStorage).getId());
                }
            });
        return serviceMetaModelRepository.persist(serviceMetaModel);
    }

    public List<ServiceMetaModel> findAll(MetaModelContext metaModelContext) {
        return elements(serviceMetaModelRepository.findAll())
            .map(serviceEntity -> serviceMetaModelMapper.toDto(metaModelContext, serviceEntity))
            .asList();
    }
}
