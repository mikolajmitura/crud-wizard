package pl.jalokim.crudwizard.genericapp.metamodel.url;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModel;

@Value
public class DataStorageMetaForUrlModel {

    DataStorageMetaModel dataStorageMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
