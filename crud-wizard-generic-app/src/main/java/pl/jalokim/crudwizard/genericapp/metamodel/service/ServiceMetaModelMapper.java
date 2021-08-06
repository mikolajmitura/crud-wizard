package pl.jalokim.crudwizard.genericapp.metamodel.service;

import static pl.jalokim.utils.collection.Elements.elements;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.provider.BeanInstanceMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ServiceMetaModelMapper extends AdditionalPropertyMapper<ServiceMetaModelDto, ServiceMetaModelEntity, ServiceMetaModel> {

    @Autowired
    private GenericBeansProvider genericBeansProvider;

    @Override
    public abstract ServiceMetaModel toMetaModel(ServiceMetaModelEntity serviceMetaModelEntity);

    public ServiceMetaModel toFullMetaModel(ServiceMetaModelEntity serviceMetaModelEntity) {
        String className = serviceMetaModelEntity.getClassName();
        String beanName = serviceMetaModelEntity.getBeanName();
        String methodName = serviceMetaModelEntity.getMethodName();

        BeanInstanceMetaModel beanInstanceMetaModel = elements(genericBeansProvider.getAllGenericServiceBeans())
            .filter(serviceBean -> serviceBean.getBeanName().equals(beanName)
                && serviceBean.getClassName().equals(className))
            .findFirst()
            .orElseGet(() -> genericBeansProvider.loadBeanInstanceFromSpringContext(className, beanName, methodName));

        BeanMethodMetaModel beanMethodMetaModel = elements(beanInstanceMetaModel.getGenericMethodMetaModels())
            .filter(methodMetaModel -> methodMetaModel.getName().equals(methodName))
            .getFirst();

        return toMetaModel(serviceMetaModelEntity).toBuilder()
            .serviceInstance(beanInstanceMetaModel.getBeanInstance())
            .methodMetaModel(beanMethodMetaModel)
            .build();
    }
}
