package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;

@MetamodelService
public class ServiceMetaModelService extends BaseService<ServiceMetaModelEntity, ServiceMetaModelRepository> {

    private final ServiceMetaModelMapper serviceMetaModelMapper;

    public ServiceMetaModelService(ServiceMetaModelRepository repository,
        ServiceMetaModelMapper serviceMetaModelMapper) {
        super(repository);
        this.serviceMetaModelMapper = serviceMetaModelMapper;
    }

    public List<ServiceMetaModel> findAllMetaModels() {
        return elements(repository.findAll())
            .map(serviceMetaModelMapper::toFullMetaModel)
            .asList();
    }

    public boolean exists(ServiceMetaModelDto serviceMetaModelDto) {
        BeanAndMethodDto serviceMethod = serviceMetaModelDto.getServiceBeanAndMethod();
        return repository.existsByBeanNameAndClassNameAndMethodName(serviceMethod.getBeanName(),
            serviceMethod.getClassName(), serviceMethod.getMethodName());
    }

    public Long createNewAndGetId(ServiceMetaModelDto serviceMetaModelDto) {
        return save(serviceMetaModelMapper.toEntity(serviceMetaModelDto)).getId();
    }
}
