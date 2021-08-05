package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.url.UrlMetamodel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    ApiTagMetamodel apiTag;

    UrlMetamodel urlMetamodel;

    HttpMethod httpMethod;

    String operationName;

    ClassMetaModel payloadMetamodel;
    ClassMetaModel queryArguments;
    ClassMetaModel pathParams;

    // TODO validation when added new service metamodel
    // verify that this bean, class, method exists
    // verify that can method arguments will be resolved correctly
    // verify that newly added serviceMetaModel does not exists already, then use existing id
    ServiceMetaModel serviceMetaModel;

    // TODO add it to Dto, Entity as well
    Boolean invokeValidation;

    EndpointResponseMetaModel responseMetaModel;

    List<DataStorageConnectorMetaModel> dataStorageConnectors;

    // TODO maybe here which headers distinct to this endpoint
    // TODO consume type and response type application/xml and application/json etc???
}
