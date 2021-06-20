package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

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
        Elements.elements(classMetaModelEntity.getGenericTypes())
            .forEach(genericTypeEntry -> {
                if (genericTypeEntry.getId() == null) {
                    genericTypeEntry.setId(saveClassModel(genericTypeEntry).getId());
                }
            });

        Elements.elements(classMetaModelEntity.getValidators())
            .forEach(validatorEntry -> {
                if (validatorEntry.getId() == null) {
                    validatorEntry.setId(validatorMetaModelRepository.persist(validatorEntry).getId());
                }
            });

        Elements.elements(classMetaModelEntity.getExtendsFromModels())
            .forEach(extendsFromEntry -> {
                if (extendsFromEntry.getId() == null) {
                    extendsFromEntry.setId(saveClassModel(extendsFromEntry).getId());
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
