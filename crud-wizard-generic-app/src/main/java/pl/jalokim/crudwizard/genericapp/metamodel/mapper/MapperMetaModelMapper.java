package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.provider.BeanInstanceMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider;
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class MapperMetaModelMapper extends AdditionalPropertyMapper<MapperMetaModelDto, MapperMetaModelEntity, MapperMetaModel> {

    @Autowired
    private GenericBeansProvider genericBeanProvider;

    @Autowired
    private InstanceLoader instanceLoader;

    @Autowired
    private BeanMethodMetaModelCreator beanMethodMetaModelCreator;

    @Override
    @Mapping(target = "mapperInstance", ignore = true)
    @Mapping(target = "methodMetaModel", ignore = true)
    public abstract MapperMetaModel toMetaModel(MapperMetaModelEntity entity);

    public MapperMetaModel toFullMetaModel(MapperMetaModelEntity mapperMetaModelEntity) {
        BeanInstanceMetaModel beanInstanceMetaModel = elements(genericBeanProvider.getAllGenericMapperBeans())
            .filter(serviceBean -> serviceBean.getBeanName().equals(mapperMetaModelEntity.getBeanName())
                && serviceBean.getClassName().equals(mapperMetaModelEntity.getClassName()))
            .getFirstOrNull();

        if (beanInstanceMetaModel == null) {
            Class<?> realClass = ClassUtils.loadRealClass(mapperMetaModelEntity.getClassName());
            Object mapperInstance = instanceLoader.createInstanceOrGetBean(mapperMetaModelEntity.getClassName());
            Method mapperMethod = findMethodByName(realClass, mapperMetaModelEntity.getMethodName());

            return toMetaModel(mapperMetaModelEntity).toBuilder()
                .mapperInstance(mapperInstance)
                .methodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(mapperMethod, realClass))
                .build();
        }

        BeanMethodMetaModel beanMethodMetaModel = elements(beanInstanceMetaModel.getGenericMethodMetaModels())
            .filter(methodMetaModel -> methodMetaModel.getName().equals(mapperMetaModelEntity.getMethodName()))
            .getFirst();

        return toMetaModel(mapperMetaModelEntity).toBuilder()
            .mapperInstance(beanInstanceMetaModel.getBeanInstance())
            .methodMetaModel(beanMethodMetaModel)
            .build();
    }
}
