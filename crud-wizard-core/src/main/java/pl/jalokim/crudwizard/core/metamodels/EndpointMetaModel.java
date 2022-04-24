package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.url.UrlMetamodel;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;

    ApiTagMetamodel apiTag;

    UrlMetamodel urlMetamodel;

    HttpMethod httpMethod;

    String operationName;

    ClassMetaModel payloadMetamodel;
    AdditionalValidatorsMetaModel payloadMetamodelAdditionalValidators;

    ClassMetaModel queryArguments;

    ClassMetaModel pathParams;

    ServiceMetaModel serviceMetaModel;

    Boolean invokeValidation;

    EndpointResponseMetaModel responseMetaModel;

    List<DataStorageResultsJoinerMetaModel> dataStorageResultsJoiners;
    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    // TODO maybe here which headers distinct to this endpoint
    // TODO #3 consume type and response type application/xml and application/json etc???
}
