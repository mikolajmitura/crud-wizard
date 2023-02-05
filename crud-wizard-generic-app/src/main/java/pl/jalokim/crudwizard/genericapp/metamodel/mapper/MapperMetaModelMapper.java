package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState.INITIALIZED;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.MAPPER_EXPECTED_ARGS_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.getCommonExpectedArgsTypeAndOther;
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
import pl.jalokim.crudwizard.genericapp.compiler.CompiledCodeMetadataDto;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
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
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperCompiledCodeMetadataEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgument;
import pl.jalokim.crudwizard.genericapp.method.BeanMethodMetaModelCreator;
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@Mapper(config = MapperAsSpringBeanConfig.class, uses = {AdditionalPropertyMapper.class, MapperGenerateConfigurationMapper.class})
@Slf4j
public abstract class MapperMetaModelMapper implements BaseMapper<MapperMetaModelDto, MapperMetaModelEntity, MapperMetaModel> {

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

        MapperMetaModel initMapperMetaModel = toMetaModel(mapperMetaModelEntity);
        MapperMetaModelBuilder<?, ?> mapperMetaModelBuilder = initMapperMetaModel.toBuilder();

        if (MapperType.GENERATED.equals(mapperMetaModelEntity.getMapperType())) {

            MapperGenerateConfigurationEntity mapperGenerateConfigurationEntity = mapperMetaModelEntity.getMapperGenerateConfiguration();
            var rootMapperConf = mapperGenerateConfigurationEntity.getRootConfiguration();
            mapperMetaModelBuilder.sourceClassMetaModel(metaModelContext.getClassMetaModels()
                .findById(rootMapperConf.getSourceMetaModel().getId()));

            mapperMetaModelBuilder.targetClassMetaModel(metaModelContext.getClassMetaModels()
                .findById(rootMapperConf.getTargetMetaModel().getId()));

        } else if (MapperType.BEAN_OR_CLASS_NAME.equals(mapperMetaModelEntity.getMapperType())) {
            BeanAndMethodEntity mapperBeanAndMethod = mapperMetaModelEntity.getMapperBeanAndMethod();

            if (mapperBeanAndMethod != null) {
                Class<?> realClass = ClassUtils.loadRealClass(mapperBeanAndMethod.getClassName());
                Object mapperInstance = instanceLoader.createInstanceOrGetBean(mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getBeanName());
                Method mapperMethod = findMethodByName(realClass, mapperBeanAndMethod.getMethodName());

                mapperMetaModelBuilder
                    .mapperInstance(mapperInstance)
                    .methodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(mapperMethod, realClass, mapperBeanAndMethod.getBeanName()));
            }
        } else {
            // TODO #53 load script
            throw new UnsupportedOperationException("Mapper script has not supported yet!");
        }
        return mapperMetaModelBuilder.build();
    }

    public MapperMetaModel toModelFromDto(MapperMetaModelDto mapperMetaModelDto) {

        if (mapperMetaModelDto == null) {
            return null;
        }

        MapperMetaModel mapperMetaModel = createMapperMetaModel(mapperMetaModelDto);

        if (mapperMetaModel != null && MetaModelState.FOR_INITIALIZE.equals(mapperMetaModel.getState())) {

            BeanAndMethodDto mapperBeanAndMethod = mapperMetaModelDto.getMapperBeanAndMethod();

            if (mapperBeanAndMethod != null) {
                mapperMetaModel.setMethodMetaModel(beanMethodMetaModelCreator.createBeanMethodMetaModel(
                    mapperBeanAndMethod.getMethodName(), mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getBeanName()));
            }

            Optional.ofNullable(mapperMetaModelDto.getMapperGenerateConfiguration())
                .map(MapperGenerateConfigurationDto::getRootConfiguration)
                .ifPresent(rootConfiguration -> {
                    mapperMetaModel.setSourceClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getSourceMetaModel()));
                    mapperMetaModel.setTargetClassMetaModel(
                        classMetaModelMapper.toModelFromDto(rootConfiguration.getTargetMetaModel()));
                });

            populateFieldsWhenBeanAndMethod(mapperMetaModelDto, mapperMetaModel);

            mapperMetaModel.setState(INITIALIZED);
        }

        return mapperMetaModel;
    }

    private void populateFieldsWhenBeanAndMethod(MapperMetaModelDto mapperMetaModelDto, MapperMetaModel mapperMetaModel) {
        Optional.ofNullable(mapperMetaModelDto.getMapperBeanAndMethod())
            .ifPresent(beanAndMethodDto -> {
                if (beanAndMethodDto.getClassName() != null && beanAndMethodDto.getMethodName() != null) {
                    try {
                        Class<?> realClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
                        Object mapperInstance = instanceLoader.createInstanceOrGetBean(beanAndMethodDto.getClassName(), beanAndMethodDto.getBeanName());
                        Method mapperMethod = findMethodByName(realClass, beanAndMethodDto.getMethodName());

                        mapperMetaModel.setMapperInstance(mapperInstance);
                        mapperMetaModel.setMethodMetaModel(beanMethodMetaModelCreator
                            .createBeanMethodMetaModel(mapperMethod, realClass, beanAndMethodDto.getBeanName()));

                        MethodSignatureMetaModel methodSignatureMetaModel = methodSignatureMetaModelResolver.getMethodSignatureMetaModel(beanAndMethodDto);

                        mapperMetaModel.setSourceClassMetaModel(findMapperInputMetaModel(methodSignatureMetaModel));
                        mapperMetaModel.setTargetClassMetaModel(methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(beanAndMethodDto));

                    } catch (Exception exception) {
                        log.warn("unexpected exception", exception);
                    }
                }
            });
    }

    private ClassMetaModel findMapperInputMetaModel(MethodSignatureMetaModel methodSignatureMetaModel) {
        ClassMetaModel foundMapperInputMetamodel = null;
        for (MethodArgumentMetaModel methodArgument : methodSignatureMetaModel.getMethodArguments()) {
            var methodArgumentPredicates = getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE);

            List<GenericMethodArgument> possibleInputOfMapperPredicates = methodArgumentPredicates.stream()
                .filter(GenericMethodArgument::isArgumentCanBeInputOfMapper)
                .collect(Collectors.toList());

            ClassMetaModel argumentClassMetaModel = classMetaModelFromType(methodArgument.getArgumentType());

            List<GenericMethodArgument> cannotBeInputOfMapperPredicates = methodArgumentPredicates.stream()
                .filter(predicate -> !predicate.isArgumentCanBeInputOfMapper())
                .collect(Collectors.toList());

            boolean resolvedAsMapperInput = isResolvedAsMapperInput(methodArgument, possibleInputOfMapperPredicates, argumentClassMetaModel);
            boolean resolvedAsOtherThanInputMapper = isResolvedAsOtherThanInputMapper(methodArgument,
                cannotBeInputOfMapperPredicates, argumentClassMetaModel);

            if (resolvedAsMapperInput && !resolvedAsOtherThanInputMapper) {
                foundMapperInputMetamodel = argumentClassMetaModel;
                break;
            }
        }
        return foundMapperInputMetamodel;
    }

    private MapperMetaModel createMapperMetaModel(MapperMetaModelDto mapperMetaModelDto) {
        MapperMetaModel mapperMetaModel = null;

        TemporaryMetaModelContext temporaryMetaModelContext = getTemporaryMetaModelContext();
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
            mapperMetaModel = temporaryMetaModelContext.findMapperMetaModelByName(mapperMetaModelDto.getMapperName());
            if (mapperMetaModel == null) {
                mapperMetaModel = innerToModelFromDto(mapperMetaModelDto);
                temporaryMetaModelContext.putToContext(mapperMetaModel.getMapperName(), mapperMetaModel);
            } else {
                swallowUpdateFrom(mapperMetaModel, mapperMetaModelDto);
            }
            mapperMetaModel.setState(MetaModelState.FOR_INITIALIZE);
        }
        return mapperMetaModel;
    }

    private boolean isResolvedAsMapperInput(MethodArgumentMetaModel methodArgument,
        List<GenericMethodArgument> possibleInputOfMapperPredicates, ClassMetaModel argumentClassMetaModel) {
        boolean resolvedAsMapperInput = true;

        for (GenericMethodArgument possibleInputOfMapperPredicate : possibleInputOfMapperPredicates) {
            resolvedAsMapperInput = true;
            if (possibleInputOfMapperPredicate.getAnnotatedWith() != null) {
                resolvedAsMapperInput = elements(methodArgument.getAnnotations())
                    .map(Annotation::annotationType)
                    .asList()
                    .contains(possibleInputOfMapperPredicate.getAnnotatedWith());
            }

            resolvedAsMapperInput = resolvedAsMapperInput && possibleInputOfMapperPredicate.getTypePredicatesAndDataExtractors().stream()
                .anyMatch(typePredicate -> argumentClassMetaModel.isSubTypeOf(typePredicate.getSubTypeOf()));

        }
        return resolvedAsMapperInput;
    }

    private boolean isResolvedAsOtherThanInputMapper(MethodArgumentMetaModel methodArgument,
        List<GenericMethodArgument> predicates, ClassMetaModel argumentClassMetaModel) {

        boolean resolvedAsOtherThanInputMapper = false;

        for (GenericMethodArgument predicate : predicates) {
            if (predicate.getAnnotatedWith() == null) {
                resolvedAsOtherThanInputMapper = true;
            } else {
                resolvedAsOtherThanInputMapper = elements(methodArgument.getAnnotations())
                    .map(Annotation::annotationType)
                    .asList()
                    .contains(predicate.getAnnotatedWith());
            }

            resolvedAsOtherThanInputMapper = resolvedAsOtherThanInputMapper && predicate.getTypePredicatesAndDataExtractors().stream()
                .anyMatch(typePredicate -> argumentClassMetaModel.isSubTypeOf(typePredicate.getSubTypeOf()));

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

    @Mapping(target = "id", ignore = true)
    protected abstract MapperCompiledCodeMetadataEntity mapToEntity(CompiledCodeMetadataDto compiledCodeMetadata);
}
