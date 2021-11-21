package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.utils.collection.Elements.elements;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.provider.BeanInstanceMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class MapperMetaModelMapper extends AdditionalPropertyMapper<MapperMetaModelDto, MapperMetaModelEntity, MapperMetaModel> {

    @Autowired
    private GenericBeansProvider genericBeanProvider;

    @Override
    @Mapping(target = "mapperInstance", ignore = true)
    @Mapping(target = "methodMetaModel", ignore = true)
    public abstract MapperMetaModel toMetaModel(MapperMetaModelEntity entity);

    public MapperMetaModel toFullMetaModel(MapperMetaModelEntity mapperMetaModelEntity) {
        BeanInstanceMetaModel beanInstanceMetaModel = elements(genericBeanProvider.getAllGenericMapperBeans())
            .filter(serviceBean -> serviceBean.getBeanName().equals(mapperMetaModelEntity.getBeanName())
                && serviceBean.getClassName().equals(mapperMetaModelEntity.getClassName()))
            .getFirst();

        BeanMethodMetaModel beanMethodMetaModel = elements(beanInstanceMetaModel.getGenericMethodMetaModels())
            .filter(methodMetaModel -> methodMetaModel.getName().equals(mapperMetaModelEntity.getMethodName()))
            .getFirst();

        return toMetaModel(mapperMetaModelEntity).toBuilder()
            .mapperInstance(beanInstanceMetaModel.getBeanInstance())
            .methodMetaModel(beanMethodMetaModel)
            .build();
    }
}
