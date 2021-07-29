package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;

@Value
@Builder(toBuilder = true)
public class FoundEndpointMetaModel {

    EndpointMetaModel endpointMetaModel;
    RawEntityObject urlPathParams;

    public boolean wasFound() {
        return Objects.nonNull(endpointMetaModel) && Objects.nonNull(urlPathParams);
    }
}
