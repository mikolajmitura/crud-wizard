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

    // TODO #1 validation when added new service metamodel
    // verify that this bean, class, method exists
    // verify that can method arguments will be resolved correctly
    // verify that newly added serviceMetaModel does not exists already, then use existing id
    ServiceMetaModel serviceMetaModel;

    Boolean invokeValidation;

    EndpointResponseMetaModel responseMetaModel;

    List<DataStorageResultsJoinerMetaModel> dataStorageResultsJoiners;
    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    // TODO maybe here which headers distinct to this endpoint
    // TODO consume type and response type application/xml and application/json etc???
}
