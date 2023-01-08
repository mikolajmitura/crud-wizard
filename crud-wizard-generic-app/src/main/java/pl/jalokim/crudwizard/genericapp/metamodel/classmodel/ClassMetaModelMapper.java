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
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel.ClassMetaModelBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;

@Mapper(config = MapperAsSpringBeanConfig.class,
    imports = {
        ClassUtils.class,
        MetaModelState.class
    })
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class ClassMetaModelMapper extends AdditionalPropertyMapper<ClassMetaModelDto, ClassMetaModelEntity, ClassMetaModel> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "realClass", ignore = true)
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "fieldMetaResolverConfigurations", ignore = true) // TODO #62 this should be stored in db? Better yes,
    // after restart application meta models should have the same fields
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "state", expression = "java(pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState.INITIALIZED)")
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

    /**
     * To simple model means without nested objects. Nested object contains only id values.
     */
    public ClassMetaModel toSimpleModel(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
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

        setupEnumMetaModelIfShould(classMetaModel);

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModel(Long id) {
        return ClassMetaModel.builder()
            .id(id)
            .build();
    }

    public ClassMetaModel toModelFromDto(ClassMetaModelDto classMetaModelDto) {

        if (classMetaModelDto == null) {
            return null;
        }

        ClassMetaModel classMetaModel = getClassMetaModel(classMetaModelDto);

        if (MetaModelState.FOR_INITIALIZE.equals(classMetaModel.getState())) {
            setupEnumMetaModelIfShould(classMetaModel);
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
                temporaryMetaModelContext.putToContext(classMetaModelDto.getName(), classMetaModel);
            }
        } else {
            classMetaModel = getClassMetaModelWhenFullDefinition(classMetaModelDto, temporaryMetaModelContext);
        }
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModelWhenFullDefinition(ClassMetaModelDto classMetaModelDto,
        TemporaryMetaModelContext temporaryMetaModelContext) {

        ClassMetaModel classMetaModel;
        if (classMetaModelDto.getName() == null) {
            classMetaModel = getClassMetaModelWhenNameNull(classMetaModelDto);
        } else {
            temporaryMetaModelContext.putDefinitionOfClassMetaModelDto(classMetaModelDto);
            classMetaModel = temporaryMetaModelContext.findClassMetaModelByName(classMetaModelDto.getName());

            if (classMetaModel == null) {
                classMetaModel = innerToModelFromDto(classMetaModelDto);
                temporaryMetaModelContext.putToContext(classMetaModelDto.getName(), classMetaModel);
            } else {
                swallowUpdateFrom(classMetaModel, classMetaModelDto);
            }
            classMetaModel.setState(MetaModelState.FOR_INITIALIZE);
        }
        return classMetaModel;
    }

    private ClassMetaModel getClassMetaModelWhenNameNull(ClassMetaModelDto classMetaModelDto) {
        ClassMetaModel classMetaModel;
        ClassMetaModelBuilder<?, ?> classBuilder = ClassMetaModel.builder();

        classBuilder.className(classMetaModelDto.getClassName())
            .realClass(loadRealClass(classMetaModelDto.getClassName()));

        if (isNotEmpty(classMetaModelDto.getGenericTypes()) ||
            isNotEmpty(classMetaModelDto.getExtendsFromModels())) {
            classBuilder.state(MetaModelState.FOR_INITIALIZE);
        }

        classMetaModel = classBuilder.build();
        return classMetaModel;
    }

    private void setupEnumMetaModelIfShould(ClassMetaModel classMetaModel) {
        Object propertyValue = classMetaModel.getPropertyRealValue(ENUM_VALUES_PREFIX);
        if (propertyValue != null) {
            classMetaModel.setEnumClassMetaModel(new EnumClassMetaModel(classMetaModel));
        }
    }

    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    @Mapping(target = "fieldMetaResolverConfigurations", ignore = true)
    protected abstract ClassMetaModel innerToModelFromDto(ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "realClass", expression = "java(ClassUtils.loadRealClass(classMetaModelDto.getClassName()))")
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "parentMetamodelCacheContext", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "fieldNames", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "fieldMetaResolverConfigurations", ignore = true)
    @Mapping(target = "attachedFieldsOwner", ignore = true)
    protected abstract void swallowUpdateFrom(@MappingTarget ClassMetaModel classMetaModel, ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    @Override
    public abstract ClassMetaModelDto toDto(ClassMetaModelEntity classMetaModelEntity);

    protected FieldMetaModel fieldToModelFromDto(FieldMetaModelDto fieldMetaModelDto,
        ClassMetaModel ownerOfField, ClassMetaModel fieldType) {
        return fieldMetaModelMapper.toModelFromDto(fieldMetaModelDto, ownerOfField, fieldType);
    }
}
