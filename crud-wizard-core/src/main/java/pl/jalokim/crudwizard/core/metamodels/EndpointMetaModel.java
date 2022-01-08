package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.url.UrlMetamodel;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointMetaModel extends AdditionalPropertyMetaModelDto {

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

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    // TODO maybe here which headers distinct to this endpoint
    // TODO consume type and response type application/xml and application/json etc???
}
