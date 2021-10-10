package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;

@RequiredArgsConstructor
@MetamodelService
public class ClassMetaModelService {

    private final ClassMetaModelRepository classMetaModelRepository;
    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelMapper classMetaModelMapper;

    public ClassMetaModelEntity saveClassModel(ClassMetaModelEntity classMetaModelEntity) {

        if (classMetaModelEntity.shouldBeSimpleRawClass()) {
            return classMetaModelRepository.findByRawClassName(classMetaModelEntity.getClassName())
                .orElseGet(() -> {
                    classMetaModelEntity.setSimpleRawClass(true);
                    return classMetaModelRepository.persist(classMetaModelEntity);
                });
        }

        elements(classMetaModelEntity.getFields())
            .forEach(field -> {
                validatorMetaModelService.saveOrCreateNewValidators(field.getValidators());
                field.setFieldType(saveClassModel(field.getFieldType()));
            });

        elements(classMetaModelEntity.getGenericTypes())
            .forEachWithIndexed(indexed -> {
                var genericTypeEntry = indexed.getValue();
                if (genericTypeEntry.getId() == null) {
                    classMetaModelEntity.getGenericTypes().set(indexed.getIndex(), saveClassModel(genericTypeEntry));
                }
            });

        validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntity.getValidators());

        elements(classMetaModelEntity.getExtendsFromModels())
            .forEachWithIndexed(indexed -> {
                var extendsFromEntry = indexed.getValue();
                if (extendsFromEntry.getId() == null) {
                    classMetaModelEntity.getExtendsFromModels().set(indexed.getIndex(), saveClassModel(extendsFromEntry));
                }
            });

        return classMetaModelRepository.persist(classMetaModelEntity);
    }

    public List<ClassMetaModel> findAllSwallowModels(MetaModelContext metaModelContext) {
        return elements(classMetaModelRepository.findAll())
            .map(entity -> classMetaModelMapper.toSwallowDto(metaModelContext, entity))
            .asList();
    }
}
