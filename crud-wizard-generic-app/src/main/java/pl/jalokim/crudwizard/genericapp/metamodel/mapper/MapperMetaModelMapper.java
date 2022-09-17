package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState.INITIALIZED;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel.MapperMetaModelBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
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

    @Autowired
    private ClassMetaModelMapper classMetaModelMapper;

    @Override
    @Mapping(target = "mapperInstance", ignore = true)
    @Mapping(target = "methodMetaModel", ignore = true)
    @Mapping(target = "targetClassMetaModel", ignore = true)
    @Mapping(target = "sourceClassMetaModel", ignore = true)
    @Mapping(target = "state", ignore = true)
    public abstract MapperMetaModel toMetaModel(MapperMetaModelEntity entity);

    // TODO #53 remove this after impl
    @Override
    @Mapping(target = "mapperScript", ignore = true)
    @Mapping(target = "metamodelDtoType", ignore = true)
    public abstract MapperMetaModelDto toDto(MapperMetaModelEntity entity);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    public abstract ClassMetaModelDto classModelToDto(ClassMetaModelEntity classMetaModelEntity);

    public MapperMetaModel toFullMetaModel(MapperMetaModelEntity mapperMetaModelEntity) {

        MapperMetaModelBuilder<?, ?> mapperMetaModelBuilder = toMetaModel(mapperMetaModelEntity).toBuilder();

        if (MapperType.GENERATED.equals(mapperMetaModelEntity.getMapperType())) {
            // TODO #1 #mapping generate new mapper when hash is the same and load new instance when should or load old.
            //  when new mapper was generated then update className in mapperMetaModelEntity
        } else if (MapperType.BEAN_OR_CLASS_NAME.equals(mapperMetaModelEntity.getMapperType())) {
            BeanAndMethodEntity mapperBeanAndMethod = mapperMetaModelEntity.getMapperBeanAndMethod();

            if (mapperBeanAndMethod != null) {
                BeanInstanceMetaModel beanInstanceMetaModel = elements(genericBeanProvider.getAllGenericMapperBeans())
                    .filter(mapperBean -> (mapperBeanAndMethod.getBeanName() == null || mapperBean.getBeanName().equals(mapperBeanAndMethod.getBeanName()))
                        && mapperBean.getClassName().equals(mapperBeanAndMethod.getClassName())
                    )
                    .getFirstOrNull();

                if (beanInstanceMetaModel == null) {
                    Class<?> realClass = ClassUtils.loadRealClass(mapperBeanAndMethod.getClassName());
                    Object mapperInstance = instanceLoader.createInstanceOrGetBean(mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getBeanName());
                    Method mapperMethod = findMethodByName(realClass, mapperBeanAndMethod.getMethodName());

                    mapperMetaModelBuilder
                        .mapperInstance(mapperInstance)
                        .methodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(mapperMethod, realClass, mapperBeanAndMethod.getBeanName()));
                } else {
                    BeanAndMethodMetaModel beanMethodMetaModel = elements(beanInstanceMetaModel.getGenericMethodMetaModels())
                        .filter(methodMetaModel -> methodMetaModel.getMethodName().equals(mapperBeanAndMethod.getMethodName()))
                        .getFirst();

                    mapperMetaModelBuilder
                        .mapperInstance(beanInstanceMetaModel.getBeanInstance())
                        .methodMetaModel(beanMethodMetaModel);
                }
            }
        } else {
            // TODO #53 load script
            throw new UnsupportedOperationException("Mapper script has not supported yet!");
        }
        return mapperMetaModelBuilder.build();
    }

    public MapperMetaModel toModelFromDto(MapperMetaModelDto mapperMetaModelDto) {

        TemporaryMetaModelContext temporaryMetaModelContext = getTemporaryMetaModelContext();

        if (mapperMetaModelDto == null) {
            return null;
        }

        MapperMetaModel mapperMetaModel = null;

        if (MetaModelDtoType.BY_ID.equals(mapperMetaModelDto.getMetamodelDtoType())) {
            mapperMetaModel = temporaryMetaModelContext.findMapperMetaModelById(mapperMetaModelDto.getId());
            if (mapperMetaModel == null) {
                throw new EntityNotFoundException(mapperMetaModelDto.getId(), MapperMetaModelEntity.class);
            }
        } else if (MetaModelDtoType.BY_NAME.equals(mapperMetaModelDto.getMetamodelDtoType())) {
            mapperMetaModel = temporaryMetaModelContext.findMapperMetaModelByName(mapperMetaModelDto.getMapperName());
            if (mapperMetaModel == null) {
                mapperMetaModel = MapperMetaModel.builder()
                    .mapperName(mapperMetaModelDto.getMapperName())
                    .state(MetaModelState.ONLY_NAME)
                    .build();
                temporaryMetaModelContext.putToContext(mapperMetaModelDto.getMapperName(), mapperMetaModel);
            }
        } else if (mapperMetaModelDto.getMapperName() != null)  {
            temporaryMetaModelContext.putDefinitionOfMapperMetaModelDto(mapperMetaModelDto);
            mapperMetaModel = temporaryMetaModelContext.findMapperMetaModelByName(mapperMetaModelDto.getMapperName());
            if (mapperMetaModel == null) {
                mapperMetaModel = innerToModelFromDto(mapperMetaModelDto);
                temporaryMetaModelContext.putToContext(mapperMetaModel.getMapperName(), mapperMetaModel);
            } else {
                swallowUpdateFrom(mapperMetaModel, mapperMetaModelDto);
            }
            mapperMetaModel.setState(MetaModelState.FOR_INITIALIZE);
        }

        if (mapperMetaModel != null && MetaModelState.FOR_INITIALIZE.equals(mapperMetaModel.getState())) {

            BeanAndMethodDto mapperBeanAndMethod = mapperMetaModelDto.getMapperBeanAndMethod();
            mapperMetaModel.setMethodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(
                mapperBeanAndMethod.getMethodName(), mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getBeanName()));

            MapperMetaModel finalMapperMetaModel = mapperMetaModel;
            Optional.ofNullable(mapperMetaModelDto.getMapperGenerateConfiguration())
                .map(MapperGenerateConfigurationDto::getRootConfiguration)
                .ifPresent(rootConfiguration -> {
                    finalMapperMetaModel.setSourceClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getSourceMetaModel()));
                    finalMapperMetaModel.setTargetClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getTargetMetaModel()));
                });


            mapperMetaModel.setState(INITIALIZED);
        }

        return mapperMetaModel;
    }

    @Mapping(target = "mapperInstance", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "sourceClassMetaModel", ignore = true)
    @Mapping(target = "targetClassMetaModel", ignore = true)
    @Mapping(target = "methodMetaModel", ignore = true)
    protected abstract MapperMetaModel innerToModelFromDto(MapperMetaModelDto mapperMetaModelDto);

    @Mapping(target = "mapperInstance", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "sourceClassMetaModel", ignore = true)
    @Mapping(target = "targetClassMetaModel", ignore = true)
    @Mapping(target = "methodMetaModel", ignore = true)
    protected abstract void swallowUpdateFrom(@MappingTarget MapperMetaModel mapperMetaModel, MapperMetaModelDto mapperMetaModelDto);
}
