package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@RequiredArgsConstructor
public class FieldMetaModelService {

    private final FieldMetaModelMapper fieldMetaModelMapper;

    public List<FieldForMergeDto> getAllFieldsForRealClass(Class<?> realClass) {
        ClassMetaModel classMetaModel = createClassMetaModel(realClass);
        return elements(classMetaModel.fetchAllFields())
            .map(fieldMetaModelMapper::mapFieldForMerge)
            .asList();
    }
}
