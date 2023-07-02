package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelsUtils.isClearRawClassFullDefinition;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;
import pl.jalokim.utils.collection.CollectionUtils;

@Mapper(config = MapperAsSpringBeanConfig.class,
    imports = {
        ClassUtils.class,
        MetaModelState.class
    },
    uses = {
        AdditionalPropertyMapper.class,
        TranslationMapper.class,
        CommonClassAndFieldMapper.class
    })
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class ClassMetaModelMapper implements BaseMapper<ClassMetaModelDto, ClassMetaModelEntity, ClassMetaModel> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Autowired
    private CommonClassAndFieldMapper commonClassAndFieldMapper;

    @Override
    public ClassMetaModelDto toDto(ClassMetaModelEntity classMetaModelEntity) {
        return commonClassAndFieldMapper.classModelToDto(classMetaModelEntity);
    }

    @Override
    public ClassMetaModelEntity toEntity(ClassMetaModelDto classMetaModelDto) {
        return commonClassAndFieldMapper.toEntity(classMetaModelDto);
    }

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "enumMetaModel", source = "classMetaModelEntity")
    @Mapping(target = "realClass", ignore = true)
    @Mapping(target = "fieldMetaResolverConfiguration", ignore = true)
    // after restart application meta models should have the same fields
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "typeMetadata", ignore = true)
    @Mapping(target = "writeFieldResolver", ignore = true)
    @Mapping(target = "readFieldResolver", ignore = true)
    @Mapping(target = "state", expression = "java(pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState.INITIALIZED)")
    public abstract ClassMetaModel toMetaModel(ClassMetaModelEntity classMetaModelEntity);

    protected abstract EnumEntryMetaModel mapEnumEntryMetaModel(EnumEntryMetaModelEntity entity);

    protected EnumMetaModel mapToEnumMetaModel(ClassMetaModelEntity classMetaModelEntity) {
        if (CollectionUtils.isNotEmpty(classMetaModelEntity.getEnums())) {
            return EnumMetaModel.builder()
                .enums(CollectionUtils.mapToList(classMetaModelEntity.getEnums(), this::mapEnumEntryMetaModel))
                .build();
        }
        return null;
    }

    /**
     * dummyFlag is not for resolving duplication of methods.
     */
    @Mapping(target = "simpleRawClass", ignore = true)
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "enums", ignore = true)
    @Mapping(target = "translationName", ignore = true)
    public abstract ClassMetaModelEntity toSimpleEntity(ClassMetaModelDto classMetaModelDto, boolean dummyFlag);

    /**
     * To simple model means without nested objects. Nested object contains only id values.
     */
    public ClassMetaModel toSimpleModel(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModel classMetaModel = toMetaModel(classMetaModelEntity);
        classMetaModel.setGenericTypes(mapToList(
            classMetaModelEntity.getGenericTypes(),
            ClassMetaModelMapper::newSimpleTemporaryClassMetaModel));

        classMetaModel.setFields(mapToList(classMetaModelEntity.getFields(),
            field -> fieldMetaModelMapper.toMetaModel(metaModelContext, classMetaModel, field)));

        classMetaModel.setValidators(mapToList(
            classMetaModelEntity.getValidators(),
            validator -> metaModelContext.getValidatorMetaModels()
                .getById(validator.getId())
        ));

        classMetaModel.setExtendsFromModels(mapToList(
            classMetaModelEntity.getExtendsFromModels(),
            ClassMetaModelMapper::newSimpleTemporaryClassMetaModel
        ));
        classMetaModel.setRealClass(loadRealClass(classMetaModelEntity.getClassName()));

        return classMetaModel;
    }

    public static ClassMetaModel newSimpleTemporaryClassMetaModel(ClassMetaModelEntity classMetaModelEntity) {
        return ClassMetaModel.builder()
            .id(classMetaModelEntity.getId())
            .name(classMetaModelEntity.getName())
            .className(classMetaModelEntity.getClassName())
            .realClass(ClassUtils.loadRealClass(classMetaModelEntity.getClassName()))
            .build();
    }

    public ClassMetaModel toModelFromDto(ClassMetaModelDto classMetaModelDto) {

        if (classMetaModelDto == null) {
            return null;
        }

        ClassMetaModel classMetaModel = getClassMetaModel(classMetaModelDto);

        if (MetaModelState.FOR_INITIALIZE.equals(classMetaModel.getState())) {
            classMetaModel.setState(MetaModelState.DURING_INITIALIZATION);
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

            classMetaModel.setFields(
                elements(classMetaModelDto.getFields())
                    .map(field -> fieldToModelFromDto(field, classMetaModel, toModelFromDto(field.getFieldType())))
                    .asList());
            classMetaModel.setState(MetaModelState.INITIALIZED);
        }
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModel(ClassMetaModelDto classMetaModelDto) {
        ClassMetaModel classMetaModel;

        TemporaryMetaModelContext temporaryMetaModelContext = getTemporaryMetaModelContext();
        if (MetaModelDtoType.BY_ID.equals(classMetaModelDto.getClassMetaModelDtoType())) {
            classMetaModel = temporaryMetaModelContext.findClassMetaModelById(classMetaModelDto.getId());
            if (classMetaModel == null) {
                throw new EntityNotFoundException(classMetaModelDto.getId(), ClassMetaModelEntity.class);
            }
        } else if (MetaModelDtoType.BY_NAME.equals(classMetaModelDto.getClassMetaModelDtoType())) {
            classMetaModel = temporaryMetaModelContext.findClassMetaModelByName(classMetaModelDto.getName());
            if (classMetaModel == null) {
                classMetaModel = ClassMetaModel.builder()
                    .name(classMetaModelDto.getName())
                    .state(MetaModelState.ONLY_NAME)
                    .build();
                temporaryMetaModelContext.putToContextByName(classMetaModelDto.getName(), classMetaModel);
            }
        } else if (MetaModelDtoType.BY_RAW_CLASSNAME.equals(classMetaModelDto.getClassMetaModelDtoType())) {
            classMetaModel = temporaryMetaModelContext.findClassMetaModelByClassName(classMetaModelDto.getClassName());
            if (classMetaModel == null) {
                classMetaModel = ClassMetaModel.builder()
                    .className(classMetaModelDto.getClassName())
                    .realClass(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))
                    .state(MetaModelState.ONLY_RAW_CLASS)
                    .build();
                temporaryMetaModelContext.putToContextByClassName(classMetaModelDto.getClassName(), classMetaModel);
            }
        } else {
            classMetaModel = getClassMetaModelWhenFullDefinition(classMetaModelDto, temporaryMetaModelContext);
        }
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModelWhenFullDefinition(ClassMetaModelDto classMetaModelDto,
        TemporaryMetaModelContext temporaryMetaModelContext) {

        ClassMetaModel classMetaModel;
        if (isNotBlank(classMetaModelDto.getClassName())) {
            classMetaModel = getClassMetaModelWhenIsByClassName(classMetaModelDto, temporaryMetaModelContext);
        } else if (isNotBlank(classMetaModelDto.getName())) {
            classMetaModel = getClassMetaModelWhenIsByName(classMetaModelDto, temporaryMetaModelContext);
        } else {
            classMetaModel = innerToModelFromDto(classMetaModelDto);
        }
        classMetaModel.setState(MetaModelState.FOR_INITIALIZE);
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModelWhenIsByName(ClassMetaModelDto classMetaModelDto, TemporaryMetaModelContext temporaryMetaModelContext) {

        temporaryMetaModelContext.putDefinitionOfClassMetaModelDto(classMetaModelDto);
        ClassMetaModel classMetaModel = temporaryMetaModelContext.findClassMetaModelByName(classMetaModelDto.getName());
        if (classMetaModel == null) {
            classMetaModel = innerToModelFromDto(classMetaModelDto);
            temporaryMetaModelContext.putToContextByName(classMetaModelDto.getName(), classMetaModel);
        } else {
            swallowUpdateFrom(classMetaModel, classMetaModelDto);
        }
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModelWhenIsByClassName(ClassMetaModelDto classMetaModelDto, TemporaryMetaModelContext temporaryMetaModelContext) {

        temporaryMetaModelContext.putDefinitionOfClassMetaModelDto(classMetaModelDto);
        ClassMetaModel classMetaModel = null;
        if (isClearRawClassFullDefinition(classMetaModelDto)) {
            classMetaModel = temporaryMetaModelContext.findClassMetaModelByClassName(classMetaModelDto.getClassName());
        }
        if (classMetaModel == null) {
            classMetaModel = innerToModelFromDto(classMetaModelDto);
            if (isClearRawClassFullDefinition(classMetaModelDto)) {
                temporaryMetaModelContext.putToContextByClassName(classMetaModelDto.getClassName(), classMetaModel);
            }
        } else {
            swallowUpdateFrom(classMetaModel, classMetaModelDto);
        }
        return classMetaModel;
    }

    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    @Mapping(target = "fieldMetaResolverConfiguration", ignore = true)
    @Mapping(target = "writeFieldResolver", ignore = true)
    @Mapping(target = "readFieldResolver", ignore = true)
    @Mapping(target = "typeMetadata", ignore = true)
    protected abstract ClassMetaModel innerToModelFromDto(ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "enumMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "fieldNames", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "fieldMetaResolverConfiguration", ignore = true)
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    @Mapping(target = "typeMetadata", ignore = true)
    @Mapping(target = "writeFieldResolver", ignore = true)
    @Mapping(target = "readFieldResolver", ignore = true)
    protected abstract void swallowUpdateFrom(@MappingTarget ClassMetaModel classMetaModel, ClassMetaModelDto classMetaModelDto);

    protected FieldMetaModel fieldToModelFromDto(FieldMetaModelDto fieldMetaModelDto,
        ClassMetaModel ownerOfField, ClassMetaModel fieldType) {
        return fieldMetaModelMapper.toModelFromDto(fieldMetaModelDto, ownerOfField, fieldType);
    }
}
