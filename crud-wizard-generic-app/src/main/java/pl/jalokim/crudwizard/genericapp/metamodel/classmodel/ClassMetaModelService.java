package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelsUtils.isClearRawClassFullDefinition;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Collection;
import java.util.List;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationService;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;

@MetamodelService
public class ClassMetaModelService extends BaseService<ClassMetaModelEntity, ClassMetaModelRepository> {

    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelMapper classMetaModelMapper;
    private final ClassMetaModelEntitySaveContext classMetaModelEntitySaveContext;
    private final TranslationService translationService;

    public ClassMetaModelService(ClassMetaModelRepository classMetaModelRepository, ValidatorMetaModelService validatorMetaModelService,
        ClassMetaModelMapper classMetaModelMapper, ClassMetaModelEntitySaveContext classMetaModelEntitySaveContext,
        TranslationService translationService) {
        super(classMetaModelRepository);
        this.validatorMetaModelService = validatorMetaModelService;
        this.classMetaModelMapper = classMetaModelMapper;
        this.classMetaModelEntitySaveContext = classMetaModelEntitySaveContext;
        this.translationService = translationService;
    }

    public void saveAsSimpleClassMetaModelEntity(ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto.isFullDefinitionType() && (classMetaModelDto.getName() != null || isClearRawClassFullDefinition(classMetaModelDto))) {
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
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "PMD.AvoidReassigningParameters"})
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

        ClassMetaModelEntity alreadyFullySavedEntity = classMetaModelEntitySaveContext.findFullySaved(classMetaModelEntity);
        if (classMetaModelEntity != alreadyFullySavedEntity) {
            return alreadyFullySavedEntity;
        }

        ClassMetaModelEntity alreadyPartiallySaved = classMetaModelEntitySaveContext.findPartiallySaved(classMetaModelEntity);
        if (classMetaModelEntity != alreadyPartiallySaved) {
            classMetaModelEntity = alreadyPartiallySaved;
        }

        if (classMetaModelEntitySaveContext.isDuringFullSave(classMetaModelEntity)) {
            return classMetaModelEntity;
        }

        return saveOthers(classMetaModelEntity, classMetaModelEntityUpdateSource);
    }

    private ClassMetaModelEntity saveOthers(ClassMetaModelEntity classMetaModelEntityToSaved, ClassMetaModelEntity classMetaModelEntityUpdateSource) {
        classMetaModelEntitySaveContext.putDuringInitializationEntity(classMetaModelEntityToSaved);
        classMetaModelEntityToSaved.setTranslationName(translationService.saveNewOrLoadById(classMetaModelEntityUpdateSource.getTranslationName()));

        invokeWhenSourceCollectionExists(classMetaModelEntityToSaved.getEnums(),
            classMetaModelEntityUpdateSource.getEnums(),
            () -> {
                elements(classMetaModelEntityUpdateSource.getEnums())
                    .forEach(enumEntry ->
                        enumEntry.setTranslation(translationService.saveNewOrLoadById(enumEntry.getTranslation()))
                    );
                classMetaModelEntityToSaved.setEnums(classMetaModelEntityUpdateSource.getEnums());
            });

        invokeWhenSourceCollectionExists(classMetaModelEntityToSaved.getFields(),
            classMetaModelEntityUpdateSource.getFields(),
            () -> {
                elements(classMetaModelEntityUpdateSource.getFields())
                    .forEach(field -> {
                        validatorMetaModelService.saveOrCreateNewValidators(field.getValidators());
                        field.setTranslationFieldName(translationService.saveNewOrLoadById(field.getTranslationFieldName()));
                        field.setFieldType(saveNewOrLoadById(field.getFieldType()));
                    });
                classMetaModelEntityToSaved.setFields(classMetaModelEntityUpdateSource.getFields());
            });

        invokeWhenSourceCollectionExists(classMetaModelEntityToSaved.getGenericTypes(),
            classMetaModelEntityUpdateSource.getGenericTypes(),
            () -> {
                elements(classMetaModelEntityUpdateSource.getGenericTypes())
                    .forEachWithIndexed(indexed -> {
                        var genericTypeEntry = indexed.getValue();
                        classMetaModelEntityUpdateSource.getGenericTypes().set(indexed.getIndex(), saveNewOrLoadById(genericTypeEntry));
                    });
                classMetaModelEntityToSaved.setGenericTypes(classMetaModelEntityUpdateSource.getGenericTypes());
            });

        invokeWhenSourceCollectionExists(classMetaModelEntityToSaved.getValidators(),
            classMetaModelEntityUpdateSource.getValidators(),
            () -> {
                validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntityUpdateSource.getValidators());
                classMetaModelEntityToSaved.setValidators(classMetaModelEntityUpdateSource.getValidators());
            });

        invokeWhenSourceCollectionExists(classMetaModelEntityToSaved.getExtendsFromModels(),
            classMetaModelEntityUpdateSource.getExtendsFromModels(),
            () -> {
                elements(classMetaModelEntityUpdateSource.getExtendsFromModels())
                    .forEachWithIndexed(indexed -> {
                        var extendsFromEntry = indexed.getValue();
                        classMetaModelEntityUpdateSource.getExtendsFromModels().set(indexed.getIndex(), saveNewOrLoadById(extendsFromEntry));
                    });
                classMetaModelEntityToSaved.setExtendsFromModels(classMetaModelEntityUpdateSource.getExtendsFromModels());
            });


        ClassMetaModelEntity savedClassMetaModelEntity = repository.save(classMetaModelEntityToSaved);
        classMetaModelEntitySaveContext.putFullySavedToContext(savedClassMetaModelEntity);

        return savedClassMetaModelEntity;
    }

    public List<ClassMetaModel> findSimpleModels(MetaModelContext metaModelContext) {
        return elements(repository.findAll())
            .map(entity -> classMetaModelMapper.toSimpleModel(metaModelContext, entity))
            .asList();
    }

    private static <T> void invokeWhenSourceCollectionExists(Collection<T> collectionToUpdate,
        Collection<T> sourceCollection, Runnable runnable) {

        if (sourceCollection == null && collectionToUpdate != null) {
            collectionToUpdate.clear();
            return;
        }
        runnable.run();
    }
}
