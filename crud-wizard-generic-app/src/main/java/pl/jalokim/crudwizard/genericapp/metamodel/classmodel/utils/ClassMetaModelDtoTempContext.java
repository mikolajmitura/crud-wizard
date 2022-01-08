package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.GenericModelTypeFactory.fromMetaModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache;

@RequiredArgsConstructor
public class ClassMetaModelDtoTempContext {

    private final Map<Long, GenericModelType> classMetaModelById = new HashMap<>();
    private final ModelsCache<ClassMetaModel> classMetaModels;

    public GenericModelType findGenericModelTypeById(Long classMetaModelId) {
        return Optional.ofNullable(classMetaModelById.get(classMetaModelId))
            .orElseGet(() -> {
                ClassMetaModel classMetaModel = classMetaModels.getById(classMetaModelId);
                return fromMetaModel(this, classMetaModel);
            });
    }

    public void updateDto(Long classMetaModelId, GenericModelType genericModelType) {
        classMetaModelById.put(classMetaModelId, genericModelType);
    }
}
