package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
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

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public ClassMetaModelEntity save(ClassMetaModelEntity classMetaModelEntity) {

        ClassMetaModelEntity alreadySaveEntityWithThatName = classMetaModelEntitySaveContext.findEntityWhenNameTheSame(classMetaModelEntity);
        if (classMetaModelEntity != alreadySaveEntityWithThatName) {
            return alreadySaveEntityWithThatName;
        }

        if (classMetaModelEntity.shouldBeSimpleRawClass()) {
            return repository.findByRawClassName(classMetaModelEntity.getClassName())
                .orElseGet(() -> {
                    classMetaModelEntity.setSimpleRawClass(true);
                    return repository.save(classMetaModelEntity);
                });
        }

        elements(classMetaModelEntity.getFields())
            .forEach(field -> {
                validatorMetaModelService.saveOrCreateNewValidators(field.getValidators());
                field.setFieldType(saveNewOrLoadById(field.getFieldType()));
            });

        elements(classMetaModelEntity.getGenericTypes())
            .forEachWithIndexed(indexed -> {
                var genericTypeEntry = indexed.getValue();
                classMetaModelEntity.getGenericTypes().set(indexed.getIndex(), saveNewOrLoadById(genericTypeEntry));
            });

        validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntity.getValidators());

        elements(classMetaModelEntity.getExtendsFromModels())
            .forEachWithIndexed(indexed -> {
                var extendsFromEntry = indexed.getValue();
                classMetaModelEntity.getExtendsFromModels().set(indexed.getIndex(), saveNewOrLoadById(extendsFromEntry));
            });

        ClassMetaModelEntity savedClassMetaModelEntity = repository.save(classMetaModelEntity);
        classMetaModelEntitySaveContext.putToContext(savedClassMetaModelEntity);

        return savedClassMetaModelEntity;
    }

    public List<ClassMetaModel> findAllSwallowModels(MetaModelContext metaModelContext) {
        return elements(repository.findAll())
            .map(entity -> classMetaModelMapper.toSwallowDto(metaModelContext, entity))
            .asList();
    }
}
