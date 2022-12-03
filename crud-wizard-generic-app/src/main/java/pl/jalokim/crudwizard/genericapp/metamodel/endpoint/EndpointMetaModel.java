package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DataStorageResultsJoinerMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModel;

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
