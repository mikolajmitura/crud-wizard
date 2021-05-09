package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class DataStorageMetaModel extends ParentMetaModel {

    Long id;

    String name;
}
