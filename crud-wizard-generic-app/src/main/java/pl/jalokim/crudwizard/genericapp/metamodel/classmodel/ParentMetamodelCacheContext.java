package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import java.util.Map;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;

@Data
class ParentMetamodelCacheContext {

    private Map<String, FieldMetaModel> fieldsByName;
    private List<FieldMetaModel> fieldMetaModels;
    private List<ValidatorMetaModel> allValidators;
}
