package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel.ServiceMetaModelBuilder;
import pl.jalokim.crudwizard.genericapp.provider.BeanInstanceMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider;

@Mapper(config = MapperAsSpringBeanConfig.class, uses = AdditionalPropertyMapper.class)
public abstract class ServiceMetaModelMapper implements BaseMapper<ServiceMetaModelDto, ServiceMetaModelEntity, ServiceMetaModel> {

    @Autowired
    private GenericBeansProvider genericBeansProvider;

    @Override
    @Mapping(target = "serviceInstance", ignore = true)
    @Mapping(target = "serviceBeanAndMethod", ignore = true)
    public abstract ServiceMetaModel toMetaModel(ServiceMetaModelEntity serviceMetaModelEntity);

    public ServiceMetaModel toFullMetaModel(ServiceMetaModelEntity serviceMetaModelEntity) {

        ServiceMetaModelBuilder<?, ?> serviceMetaModelBuilder = toMetaModel(serviceMetaModelEntity).toBuilder();

        BeanAndMethodEntity serviceMethod = serviceMetaModelEntity.getServiceBeanAndMethod();
        if (serviceMethod != null && elements(serviceMethod.getClassName(), serviceMethod.getMethodName())
            .allMatch(Objects::nonNull)) {

            String className = serviceMethod.getClassName();
            String beanName = serviceMethod.getBeanName();
            String methodName = serviceMethod.getMethodName();

            BeanInstanceMetaModel beanInstanceMetaModel = elements(genericBeansProvider.getAllGenericServiceBeans())
                .filter(serviceBean -> (beanName == null || serviceBean.getBeanName().equals(beanName)) &&
                    serviceBean.getClassName().equals(className))
                .findFirst()
                .orElseGet(() -> genericBeansProvider.loadBeanInstanceFromSpringContext(className, beanName, methodName));

            BeanAndMethodMetaModel beanMethodMetaModel = elements(beanInstanceMetaModel.getGenericMethodMetaModels())
                .filter(methodMetaModel -> methodMetaModel.getMethodName().equals(methodName))
                .getFirst();

            serviceMetaModelBuilder = serviceMetaModelBuilder
                .serviceInstance(beanInstanceMetaModel.getBeanInstance())
                .serviceBeanAndMethod(beanMethodMetaModel);
        }

        return serviceMetaModelBuilder.build();
    }
}
