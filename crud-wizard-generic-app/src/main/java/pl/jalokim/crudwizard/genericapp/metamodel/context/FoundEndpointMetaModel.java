package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;

@Value
@Builder(toBuilder = true)
public class FoundEndpointMetaModel {

    EndpointMetaModel endpointMetaModel;
    Map<String, Object> urlPathParams;

    public boolean isFound() {
        return Objects.nonNull(endpointMetaModel) && Objects.nonNull(urlPathParams);
    }

    public boolean isNotFound() {
        return !isFound();
    }
}
