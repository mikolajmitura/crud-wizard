package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;
import pl.jalokim.utils.collection.Elements;

@RequiredArgsConstructor
@MetamodelService
public class ServiceMetaModelService {

    private final ServiceMetaModelRepository serviceMetaModelRepository;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final ClassMetaModelService classMetaModelService;

    public ServiceMetaModelEntity saveServiceMetaModel(ServiceMetaModelEntity serviceMetaModel) {
        Elements.elements(serviceMetaModel.getDataStorageConnectors())
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
}
