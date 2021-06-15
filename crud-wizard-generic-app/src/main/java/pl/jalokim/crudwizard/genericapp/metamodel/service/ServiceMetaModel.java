package pl.jalokim.crudwizard.genericapp.metamodel.service;

import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageConnectorMetaModel;
import pl.jalokim.utils.collection.Elements;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ServiceMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    String realClassName;
    String realMethodName;
    String serviceScript;

    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    public boolean isGenericServiceMetaModel() {
        return Elements.elements(realClassName, realMethodName, serviceScript)
            .allMatch(Objects::isNull);
    }
}
