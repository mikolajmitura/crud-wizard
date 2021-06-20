package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    ApiTagMetamodel apiTag;

    String baseUrl;

    HttpMethod httpMethod;

    String operationName;

    ClassMetaModel payloadMetamodel;
    List<ClassMetaModel> queryArguments;

    ServiceMetaModel serviceMetaModel;

    EndpointResponseMetaModel responseMetaModel;

    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    // TODO maybe here which headers distinct to this endpoint
    // TODO below should be translated whole url
}
