package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ServiceMetaModelMapper extends AdditionalPropertyMapper<ServiceMetaModel, ServiceMetaModelEntity> {

    @Autowired
    private DataStorageConnectorMetaModelMapper dataStorageConnectorMetaModelMapper;

    @Override
    @Mapping(target = "dataStorageConnectors", ignore = true)
    public abstract ServiceMetaModel toDto(ServiceMetaModelEntity serviceMetaModelEntity);

    public ServiceMetaModel toDto(MetaModelContext metaModelContext, ServiceMetaModelEntity serviceMetaModelEntity) {
        return toDto(serviceMetaModelEntity).toBuilder()
            .dataStorageConnectors(elements(serviceMetaModelEntity.getDataStorageConnectors())
                .map(dataStorageConnectorEntity ->
                    dataStorageConnectorMetaModelMapper.toDto(metaModelContext, dataStorageConnectorEntity))
                .asList())
            .build();
    }
}
