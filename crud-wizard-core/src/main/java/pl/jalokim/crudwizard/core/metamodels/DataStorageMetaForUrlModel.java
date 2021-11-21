package pl.jalokim.crudwizard.core.metamodels;

import lombok.Value;

@Value
public class DataStorageMetaForUrlModel {

    DataStorageMetaModel dataStorageMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
