package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
class ParentMetamodelCacheContext {

    private Map<String, FieldMetaModel> fieldsByName;
    private List<FieldMetaModel> fieldMetaModels;
    private List<ValidatorMetaModel> allValidators;
}
