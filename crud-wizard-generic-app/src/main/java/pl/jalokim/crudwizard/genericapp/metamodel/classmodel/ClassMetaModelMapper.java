package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumClassMetaModel.ENUM_VALUES_PREFIX;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel.ClassMetaModelBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class,
    imports = {
        ClassUtils.class,
        ClassMetaModelState.class
    })
public abstract class ClassMetaModelMapper extends AdditionalPropertyMapper<ClassMetaModelDto, ClassMetaModelEntity, ClassMetaModel> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "realClass", ignore = true)
    @Mapping(target = "basedOnClass", ignore = true)
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "state", expression = "java(ClassMetaModelState.INITIALIZED)")
    public abstract ClassMetaModel toMetaModel(ClassMetaModelEntity classMetaModelEntity);

    @Mapping(target = "simpleRawClass", ignore = true)
    @Override
    public abstract ClassMetaModelEntity toEntity(ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "simpleRawClass", ignore = true)
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    public abstract ClassMetaModelEntity toSimpleEntity(ClassMetaModelDto classMetaModelDto, boolean dummyFlag);

    public ClassMetaModel toSwallowDto(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModel classMetaModel = toMetaModel(classMetaModelEntity);
        classMetaModel.setGenericTypes(mapToList(
            classMetaModelEntity.getGenericTypes(),
            genericType -> newClassMetaModel(genericType.getId())));

        classMetaModel.setFields(mapToList(classMetaModelEntity.getFields(),
            field -> fieldMetaModelMapper.toMetaModel(metaModelContext, classMetaModel, field)));

        classMetaModel.setValidators(mapToList(
            classMetaModelEntity.getValidators(),
            validator -> metaModelContext.getValidatorMetaModels()
                .getById(validator.getId())
        ));

        classMetaModel.setExtendsFromModels(mapToList(
            classMetaModelEntity.getExtendsFromModels(),
            extendsFromModel -> newClassMetaModel(extendsFromModel.getId())
        ));
        classMetaModel.setRealClass(loadRealClass(classMetaModelEntity.getClassName()));
        classMetaModel.setBasedOnClass(loadRealClass(classMetaModelEntity.getBasedOnClass()));

        setupEnumMetaModelIfShould(classMetaModel);

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModel(Long id) {
        return ClassMetaModel.builder()
            .id(id)
            .build();
    }

    public ClassMetaModel toModelFromDto(ClassMetaModelDto classMetaModelDto) {
        TemporaryMetaModelContext temporaryMetaModelContext = getTemporaryMetaModelContext();

        if (classMetaModelDto == null) {
            return null;
        }

        ClassMetaModel classMetaModel;

        if (ClassMetaModelDtoType.BY_ID.equals(classMetaModelDto.getClassMetaModelDtoType())) {
            classMetaModel = temporaryMetaModelContext.findById(classMetaModelDto.getId());
            if (classMetaModel == null) {
                throw new EntityNotFoundException(classMetaModelDto.getId(), ClassMetaModelEntity.class);
            }
        } else if (ClassMetaModelDtoType.BY_NAME.equals(classMetaModelDto.getClassMetaModelDtoType())) {
            classMetaModel = temporaryMetaModelContext.findByName(classMetaModelDto.getName());
            if (classMetaModel == null) {
                classMetaModel = ClassMetaModel.builder()
                    .name(classMetaModelDto.getName())
                    .state(ClassMetaModelState.ONLY_NAME)
                    .build();
                temporaryMetaModelContext.put(classMetaModelDto.getName(), classMetaModel);
            }
        } else {
            if (classMetaModelDto.getName() != null) {
                temporaryMetaModelContext.putDefinitionOfClassMetaModelDto(classMetaModelDto);
                classMetaModel = temporaryMetaModelContext.findByName(classMetaModelDto.getName());

                if (classMetaModel == null) {
                    classMetaModel = innerToModelFromDto(classMetaModelDto);
                    temporaryMetaModelContext.put(classMetaModelDto.getName(), classMetaModel);
                } else {
                    swallowUpdateFrom(classMetaModel, classMetaModelDto);
                }
                classMetaModel.setState(ClassMetaModelState.FOR_INITIALIZE);
            } else {
                ClassMetaModelBuilder<?, ?> classBuilder = ClassMetaModel.builder();

                classBuilder.className(classMetaModelDto.getClassName())
                    .realClass(ClassUtils.loadRealClass(classMetaModelDto.getClassName()));

                if (isNotEmpty(classMetaModelDto.getGenericTypes())
                    || isNotEmpty(classMetaModelDto.getExtendsFromModels())) {
                    classBuilder.state(ClassMetaModelState.FOR_INITIALIZE);
                }

                classMetaModel = classBuilder.build();
            }
        }

        if (ClassMetaModelState.FOR_INITIALIZE.equals(classMetaModel.getState())) {
            setupEnumMetaModelIfShould(classMetaModel);
            classMetaModel.setState(ClassMetaModelState.DURING_INITIALIZATION);
            classMetaModel.setGenericTypes(
                elements(classMetaModelDto.getGenericTypes())
                    .map(this::toModelFromDto)
                    .asList()
            );

            classMetaModel.setExtendsFromModels(
                elements(classMetaModelDto.getExtendsFromModels())
                    .map(this::toModelFromDto)
                    .asList()
            );

            ClassMetaModel ownerOfField = classMetaModel;

            classMetaModel.setFields(
                elements(classMetaModelDto.getFields())
                    .map(field -> (FieldMetaModel) FieldMetaModel.builder()
                        .fieldName(field.getFieldName())
                        .ownerOfField(ownerOfField)
                        .fieldType(toModelFromDto(field.getFieldType()))
                        .additionalProperties(elements(field.getAdditionalProperties())
                        .map(this::additionalPropertyToModel)
                        .asList())
                        .build()
                    )
                    .asList());
            classMetaModel.setState(ClassMetaModelState.INITIALIZED);
        }
        return classMetaModel;
    }

    private void setupEnumMetaModelIfShould(ClassMetaModel classMetaModel) {
        Object propertyValue = classMetaModel.getPropertyRealValue(ENUM_VALUES_PREFIX);
        if (propertyValue != null) {
            classMetaModel.setEnumClassMetaModel(new EnumClassMetaModel(classMetaModel));
        }
    }

    @Mapping(target = "basedOnClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getBasedOnClass()))")
    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "state", ignore = true)
    protected abstract ClassMetaModel innerToModelFromDto(ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "basedOnClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getBasedOnClass()))")
    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "fieldNames", ignore = true)
    @Mapping(target = "state", ignore = true)
    protected abstract void swallowUpdateFrom(@MappingTarget ClassMetaModel classMetaModel, ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    public abstract ClassMetaModelDto toDto(ClassMetaModelEntity classMetaModelEntity);
}
