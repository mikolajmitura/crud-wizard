package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

@RequiredArgsConstructor
public class TemporaryMetaModelContext {

    private final MetaModelContext metaModelContext;
    private final ObjectCache<String, ClassMetaModel> classMetaModelsByName = new ObjectCache<>();
    private final Map<String, ClassMetaModelDto> classMetaModelDtoDefinitionByName = new HashMap<>();

    public ClassMetaModel findById(Long id) {
        return metaModelContext.getClassMetaModels()
        .findById(id);
    }

    public ClassMetaModel findByName(String name) {
        return Optional.ofNullable(classMetaModelsByName
            .findById(name))
            .orElseGet(()-> metaModelContext.getClassMetaModels().findOneBy(
                givenClassModel -> name.equals(givenClassModel.getName())));
    }

    public void put(String name, ClassMetaModel classMetaModel) {
        classMetaModelsByName.put(name, classMetaModel);
    }

    public void putDefinitionOfClassMetaModelDto(ClassMetaModelDto classMetaModelDto) {
        classMetaModelDtoDefinitionByName.put(classMetaModelDto.getName(), classMetaModelDto);
    }

    public List<ClassMetaModelDto> getAllClassMetaModelDtoDefinitions() {
        return new ArrayList<>(classMetaModelDtoDefinitionByName.values());
    }
}
