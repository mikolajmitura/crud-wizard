package pl.jalokim.crudwizard.genericapp.metamodel.service;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.utils.collection.Elements;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
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
