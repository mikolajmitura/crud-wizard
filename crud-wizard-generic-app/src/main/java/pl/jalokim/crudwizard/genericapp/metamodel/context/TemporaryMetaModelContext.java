package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@RequiredArgsConstructor
public class TemporaryMetaModelContext {

    private final MetaModelContext metaModelContext;
    private final ObjectCache<String, ClassMetaModel> classMetaModelsByName = new ObjectCache<>();

    public ClassMetaModel findById(Long id) {
        return metaModelContext.getClassMetaModels()
        .findById(id);
    }

    public ClassMetaModel findByName(String name) {
        return Optional.ofNullable(classMetaModelsByName
            .findById(name))
            .orElse(metaModelContext.getClassMetaModels().findOneBy(
                givenClassModel -> name.equals(givenClassModel.getName())));
    }

    public void put(String name, ClassMetaModel classMetaModel) {
        classMetaModelsByName.put(name, classMetaModel);
    }
}
