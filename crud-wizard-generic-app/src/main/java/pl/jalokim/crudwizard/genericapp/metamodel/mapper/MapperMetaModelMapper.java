package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState.INITIALIZED;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.MAPPER_EXPECTED_ARGS_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.getCommonExpectedArgsTypeAndOther;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel.MapperMetaModelBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.ExpectedMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.BeanInstanceMetaModel;
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider;
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator;
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@Mapper(config = MapperAsSpringBeanConfig.class)
@Slf4j
public abstract class MapperMetaModelMapper extends AdditionalPropertyMapper<MapperMetaModelDto, MapperMetaModelEntity, MapperMetaModel> {

    @Autowired
    private GenericBeansProvider genericBeanProvider;

    @Autowired
    private InstanceLoader instanceLoader;

    @Autowired
    private BeanMethodMetaModelCreator beanMethodMetaModelCreator;

    @Autowired
    private ClassMetaModelMapper classMetaModelMapper;

    @Autowired
    private MethodSignatureMetaModelResolver methodSignatureMetaModelResolver;

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

    public MapperMetaModel toFullMetaModel(MapperMetaModelEntity mapperMetaModelEntity, MetaModelContext metaModelContext) {

        MapperMetaModelBuilder<?, ?> mapperMetaModelBuilder = toMetaModel(mapperMetaModelEntity).toBuilder();

        if (MapperType.GENERATED.equals(mapperMetaModelEntity.getMapperType())) {

            var rootMapperConf = mapperMetaModelEntity.getMapperGenerateConfiguration().getRootConfiguration();
            mapperMetaModelBuilder.sourceClassMetaModel(metaModelContext.getClassMetaModels()
                .findById(rootMapperConf.getSourceMetaModel().getId()));

            mapperMetaModelBuilder.targetClassMetaModel(metaModelContext.getClassMetaModels()
                .findById(rootMapperConf.getTargetMetaModel().getId()));
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
        } else if (mapperMetaModelDto.getMapperName() != null) {
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

            if (mapperBeanAndMethod != null) {
                mapperMetaModel.setMethodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(
                    mapperBeanAndMethod.getMethodName(), mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getBeanName()));
            }

            MapperMetaModel finalMapperMetaModel = mapperMetaModel;
            Optional.ofNullable(mapperMetaModelDto.getMapperGenerateConfiguration())
                .map(MapperGenerateConfigurationDto::getRootConfiguration)
                .ifPresent(rootConfiguration -> {
                    finalMapperMetaModel.setSourceClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getSourceMetaModel()));
                    finalMapperMetaModel.setTargetClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getTargetMetaModel()));
                });

            Optional.ofNullable(mapperMetaModelDto.getMapperBeanAndMethod())
                .ifPresent(beanAndMethodDto -> {
                    if (beanAndMethodDto.getClassName() != null && beanAndMethodDto.getMethodName() != null) {
                        try {
                            Class<?> realClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
                            Object mapperInstance = instanceLoader.createInstanceOrGetBean(beanAndMethodDto.getClassName(), beanAndMethodDto.getBeanName());
                            Method mapperMethod = findMethodByName(realClass, beanAndMethodDto.getMethodName());

                            finalMapperMetaModel.setMapperInstance(mapperInstance);
                            finalMapperMetaModel.setMethodMetaModel(beanMethodMetaModelCreator
                                .createBeanMethodMetaModel(mapperMethod, realClass, beanAndMethodDto.getBeanName()));

                            MethodSignatureMetaModel methodSignatureMetaModel = methodSignatureMetaModelResolver.getMethodSignatureMetaModel(beanAndMethodDto);

                            ClassMetaModel foundMapperInputMetamodel = null;
                            for (MethodArgumentMetaModel methodArgument : methodSignatureMetaModel.getMethodArguments()) {
                                var methodArgumentPredicates = getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE);

                                List<ExpectedMethodArgument> canBeInputOfMapperPredicates = methodArgumentPredicates.stream()
                                    .filter(ExpectedMethodArgument::isArgumentCanBeInputOfMapper)
                                    .collect(Collectors.toList());

                                ClassMetaModel argumentClassMetaModel = classMetaModelFromType(methodArgument.getArgumentType());

                                List<ExpectedMethodArgument> cannotBeInputOfMapperPredicates = methodArgumentPredicates.stream()
                                    .filter(predicate -> !predicate.isArgumentCanBeInputOfMapper())
                                    .collect(Collectors.toList());


                                boolean resolvedAsMapperInput = isResolvedAsMapperInput(methodArgument, canBeInputOfMapperPredicates, argumentClassMetaModel);
                                boolean resolvedAsOtherThanInputMapper = isResolvedAsOtherThanInputMapper(methodArgument,
                                    cannotBeInputOfMapperPredicates, argumentClassMetaModel);

                                if (resolvedAsMapperInput && !resolvedAsOtherThanInputMapper) {
                                    foundMapperInputMetamodel = argumentClassMetaModel;
                                    break;
                                }
                            }

                            finalMapperMetaModel.setSourceClassMetaModel(foundMapperInputMetamodel);
                            finalMapperMetaModel.setTargetClassMetaModel(methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(beanAndMethodDto));

                        } catch (Exception exception) {
                            log.warn("unexpected exception", exception);
                        }
                    }
                });

            mapperMetaModel.setState(INITIALIZED);
        }

        return mapperMetaModel;
    }

    private boolean isResolvedAsMapperInput(MethodArgumentMetaModel methodArgument,
        List<ExpectedMethodArgument> canBeInputOfMapperPredicates, ClassMetaModel argumentClassMetaModel) {
        boolean resolvedAsMapperInput = true;

        for (ExpectedMethodArgument canBeInputOfMapperPredicate : canBeInputOfMapperPredicates) {
            resolvedAsMapperInput = true;
            if (canBeInputOfMapperPredicate.getIsAnnotatedWith() != null) {
                resolvedAsMapperInput = elements(methodArgument.getAnnotations())
                        .map(Annotation::annotationType)
                        .asList()
                        .contains(canBeInputOfMapperPredicate.getIsAnnotatedWith());
            }

            resolvedAsMapperInput = resolvedAsMapperInput && canBeInputOfMapperPredicate.getTypePredicates().stream()
                .anyMatch(typePredicate -> argumentClassMetaModel.isSubTypeOf(typePredicate.getIsSubTypeOf()));

        }
        return resolvedAsMapperInput;
    }
    private boolean isResolvedAsOtherThanInputMapper(MethodArgumentMetaModel methodArgument,
        List<ExpectedMethodArgument> predicates, ClassMetaModel argumentClassMetaModel) {

        boolean resolvedAsOtherThanInputMapper = false;

        for (ExpectedMethodArgument predicate : predicates) {
            if (predicate.getIsAnnotatedWith() != null) {
                resolvedAsOtherThanInputMapper = elements(methodArgument.getAnnotations())
                        .map(Annotation::annotationType)
                        .asList()
                        .contains(predicate.getIsAnnotatedWith());
            } else {
                resolvedAsOtherThanInputMapper = true;
            }

            resolvedAsOtherThanInputMapper = resolvedAsOtherThanInputMapper && predicate.getTypePredicates().stream()
                .anyMatch(typePredicate -> argumentClassMetaModel.isSubTypeOf(typePredicate.getIsSubTypeOf()));

            if (resolvedAsOtherThanInputMapper) {
                break;
            }
        }
        return resolvedAsOtherThanInputMapper;
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
