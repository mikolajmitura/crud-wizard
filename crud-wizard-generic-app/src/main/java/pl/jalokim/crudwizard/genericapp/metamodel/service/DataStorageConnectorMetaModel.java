package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class DataStorageConnectorMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;
    MapperMetaModel mapperMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
