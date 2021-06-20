package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@RequiredArgsConstructor
@MetamodelService
public class ServiceMetaModelService {

    private final ServiceMetaModelRepository serviceMetaModelRepository;
    private final ServiceMetaModelMapper serviceMetaModelMapper;

    public ServiceMetaModelEntity saveServiceMetaModel(ServiceMetaModelEntity serviceMetaModel) {
        return serviceMetaModelRepository.persist(serviceMetaModel);
    }

    public List<ServiceMetaModel> findAllMetaModels() {
        return elements(serviceMetaModelRepository.findAll())
            .map(serviceMetaModelMapper::toFullMetaModel)
            .asList();
    }

    public boolean exists(ServiceMetaModelDto serviceMetaModelDto) {
        return serviceMetaModelRepository.existsByBeanNameAndClassNameAndMethodName(serviceMetaModelDto.getBeanName(),
            serviceMetaModelDto.getClassName(), serviceMetaModelDto.getMethodName());
    }

    public Long createNewAndGetId(ServiceMetaModelDto serviceMetaModelDto) {
        return saveServiceMetaModel(serviceMetaModelMapper.toEntity(serviceMetaModelDto)).getId();
    }

}
