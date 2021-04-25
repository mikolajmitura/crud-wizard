package pl.jalokim.crudwizard.genericapp.metamodel.service;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.utils.collection.Elements;

@Value
@Builder
public class ServiceMetaModel {

    Long serviceMetaModelId;
    Long serviceRealClassId;
    Long serviceMethodId;
    String serviceScript;

    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    public boolean isGenericServiceMetaModel() {
        return Elements.elements(serviceRealClassId, serviceMethodId, serviceScript)
            .allMatch(Objects::isNull);
    }
}
