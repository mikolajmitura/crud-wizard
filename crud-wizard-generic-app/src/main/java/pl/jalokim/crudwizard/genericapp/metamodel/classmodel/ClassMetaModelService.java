package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelRepository;
import pl.jalokim.utils.collection.Elements;

@RequiredArgsConstructor
@MetamodelService
public class ClassMetaModelService {

    private final ClassMetaModelRepository classMetaModelRepository;
    private final ValidatorMetaModelRepository validatorMetaModelRepository;
    private final ClassMetaModelMapper classMetaModelMapper;

    public ClassMetaModelEntity saveClassModel(ClassMetaModelEntity classMetaModelEntity) {
        nullableElements(classMetaModelEntity.getFields())
            .forEach(field -> field.setFieldType(saveClassModel(field.getFieldType())));

        nullableElements(classMetaModelEntity.getGenericTypes())
            .forEachWithIndexed(indexed -> {
                var genericTypeEntry = indexed.getValue();
                if (genericTypeEntry.getId() == null) {
                    classMetaModelEntity.getGenericTypes().set(indexed.getIndex(), saveClassModel(genericTypeEntry));
                }
            });

        nullableElements(classMetaModelEntity.getValidators())
            .forEachWithIndexed(indexed -> {
                var validatorEntry = indexed.getValue();
                if (validatorEntry.getId() == null) {
                    classMetaModelEntity.getValidators().set(indexed.getIndex(), validatorMetaModelRepository.persist(validatorEntry));
                }
            });

        nullableElements(classMetaModelEntity.getExtendsFromModels())
            .forEachWithIndexed(indexed -> {
                var extendsFromEntry = indexed.getValue();
                if (extendsFromEntry.getId() == null) {
                    classMetaModelEntity.getExtendsFromModels().set(indexed.getIndex(), saveClassModel(extendsFromEntry));
                }
            });

        return classMetaModelRepository.persist(classMetaModelEntity);
    }

    public List<ClassMetaModel> findAllSwallowModels(MetaModelContext metaModelContext) {
        return Elements.elements(classMetaModelRepository.findAll())
            .map(entity -> classMetaModelMapper.toSwallowDto(metaModelContext, entity))
            .asList();
    }
}
