package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;

@MetamodelService
public class ClassMetaModelService extends BaseService<ClassMetaModelEntity, ClassMetaModelRepository> {

    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelMapper classMetaModelMapper;
    private final ClassMetaModelEntitySaveContext classMetaModelEntitySaveContext;

    public ClassMetaModelService(ClassMetaModelRepository classMetaModelRepository, ValidatorMetaModelService validatorMetaModelService,
        ClassMetaModelMapper classMetaModelMapper, ClassMetaModelEntitySaveContext classMetaModelEntitySaveContext) {
        super(classMetaModelRepository);
        this.validatorMetaModelService = validatorMetaModelService;
        this.classMetaModelMapper = classMetaModelMapper;
        this.classMetaModelEntitySaveContext = classMetaModelEntitySaveContext;
    }

    public void saveAsSimpleClassMetaModelEntity(ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto.isFullDefinitionType() && classMetaModelDto.getName() != null) {
            classMetaModelEntitySaveContext.putPartiallySavedToContext(classMetaModelDto);
        }
        elements(classMetaModelDto.getGenericTypes())
            .forEach(this::saveAsSimpleClassMetaModelEntity);

        elements(classMetaModelDto.getExtendsFromModels())
            .forEach(this::saveAsSimpleClassMetaModelEntity);

        elements(classMetaModelDto.getFields())
            .map(FieldMetaModelDto::getFieldType)
            .forEach(this::saveAsSimpleClassMetaModelEntity);
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public ClassMetaModelEntity save(ClassMetaModelEntity classMetaModelEntity) {
        var temporaryMetaModelContext = TemporaryModelContextHolder.getTemporaryMetaModelContext();
        ClassMetaModelDto fullDtoDefinition = temporaryMetaModelContext.getClassMetaModelDtoByName(classMetaModelEntity.getName());
        ClassMetaModelEntity classMetaModelEntityUpdateSource;
        if (fullDtoDefinition == null) {
            ClassMetaModel classMetaModelByName = temporaryMetaModelContext.findClassMetaModelByName(classMetaModelEntity.getName());
            if (classMetaModelByName != null) {
                return repository.getOne(classMetaModelByName.getId());
            }
            classMetaModelEntityUpdateSource = classMetaModelEntity;
        } else {
            classMetaModelEntityUpdateSource = classMetaModelMapper.toEntity(fullDtoDefinition);
        }

        if (classMetaModelEntity.shouldBeSimpleRawClass()) {
            return repository.findByRawClassName(classMetaModelEntity.getClassName())
                .orElseGet(() -> {
                    classMetaModelEntityUpdateSource.setSimpleRawClass(true);
                    return repository.save(classMetaModelEntityUpdateSource);
                });
        }

        ClassMetaModelEntity alreadyFullySavedEntityWithThatName = classMetaModelEntitySaveContext
            .findFullySavedWhenNameTheSame(classMetaModelEntity);
        if (classMetaModelEntity != alreadyFullySavedEntityWithThatName) {
            return alreadyFullySavedEntityWithThatName;
        }

        ClassMetaModelEntity alreadyPartiallySavedEntityWithThatName = classMetaModelEntitySaveContext
            .findPartiallySavedWhenNameTheSame(classMetaModelEntity);

        if (classMetaModelEntity != alreadyPartiallySavedEntityWithThatName) {
            classMetaModelEntity = alreadyPartiallySavedEntityWithThatName;
        }

        if (classMetaModelEntitySaveContext.isDuringFullSave(classMetaModelEntity)) {
            return classMetaModelEntity;
        }

        return saveOthers(classMetaModelEntity, classMetaModelEntityUpdateSource);
    }

    private ClassMetaModelEntity saveOthers(ClassMetaModelEntity classMetaModelEntityToSaved, ClassMetaModelEntity classMetaModelEntityUpdateSource) {
        classMetaModelEntitySaveContext.putDuringInitializationEntity(classMetaModelEntityToSaved);
        elements(classMetaModelEntityUpdateSource.getFields())
            .forEach(field -> {
                validatorMetaModelService.saveOrCreateNewValidators(field.getValidators());
                field.setFieldType(saveNewOrLoadById(field.getFieldType()));
            });
        classMetaModelEntityToSaved.setFields(classMetaModelEntityUpdateSource.getFields());

        elements(classMetaModelEntityUpdateSource.getGenericTypes())
            .forEachWithIndexed(indexed -> {
                var genericTypeEntry = indexed.getValue();
                classMetaModelEntityUpdateSource.getGenericTypes().set(indexed.getIndex(), saveNewOrLoadById(genericTypeEntry));
            });
        classMetaModelEntityToSaved.setGenericTypes(classMetaModelEntityUpdateSource.getGenericTypes());

        validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntityUpdateSource.getValidators());
        classMetaModelEntityToSaved.setValidators(classMetaModelEntityUpdateSource.getValidators());

        elements(classMetaModelEntityUpdateSource.getExtendsFromModels())
            .forEachWithIndexed(indexed -> {
                var extendsFromEntry = indexed.getValue();
                classMetaModelEntityUpdateSource.getExtendsFromModels().set(indexed.getIndex(), saveNewOrLoadById(extendsFromEntry));
            });
        classMetaModelEntityToSaved.setExtendsFromModels(classMetaModelEntityUpdateSource.getExtendsFromModels());

        ClassMetaModelEntity savedClassMetaModelEntity = repository.save(classMetaModelEntityToSaved);
        classMetaModelEntitySaveContext.putFullySavedToContext(savedClassMetaModelEntity);

        return savedClassMetaModelEntity;
    }

    public List<ClassMetaModel> findSimpleModels(MetaModelContext metaModelContext) {
        return elements(repository.findAll())
            .map(entity -> classMetaModelMapper.toSimpleModel(metaModelContext, entity))
            .asList();
    }
}
