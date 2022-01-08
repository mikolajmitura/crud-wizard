package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EQUAL_TO_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.DataStorageResultsJoinCorrectness;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.PathParamsAndUrlVariablesTheSame;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldShouldWhenOther(field = "payloadMetamodel", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"POST", "PUT", "PATCH"})
@FieldShouldWhenOther(field = "responseMetaModel", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"GET", "POST"})
@FieldShouldWhenOther(field = "payloadMetamodel", should = NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"GET", "DELETE"})
@FieldShouldWhenOther(field = "pathParams", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"PUT", "PATCH"})
@FieldShouldWhenOther(field = "dataStorageResultsJoiners", should = EMPTY_OR_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = NOT_EQUAL_TO_ALL, otherFieldValues = {"GET"})
@FieldShouldWhenOther(field = "dataStorageConnectors", should = NOT_EMPTY, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"DELETE"})
@PathParamsAndUrlVariablesTheSame
@EndpointNotExistsAlready
@DataStorageResultsJoinCorrectness
public class EndpointMetaModelDto extends AdditionalPropertyMetaModelDto {

    public static final String HTTP_METHOD = "httpMethod";

    @NotNull(groups = EndpointUpdateContext.class)
    Long id;

    @NotNull
    ApiTagDto apiTag;

    @NotNull
    String baseUrl;

    @NotNull
    HttpMethod httpMethod;

    @NotNull
    // TODO #44 verify that operationName is unique globally when create
    String operationName;

    @Valid
    ClassMetaModelDto payloadMetamodel;

    List<@Valid AdditionalValidatorsMetaModelDto> payloadMetamodelAdditionalValidators;

    @Valid
    ClassMetaModelDto queryArguments;

    @Valid
    ClassMetaModelDto pathParams;

    @Valid
    ServiceMetaModelDto serviceMetaModel;

    Boolean invokeValidation;

    @Valid
    EndpointResponseMetaModelDto responseMetaModel;

    List<@Valid DataStorageConnectorMetaModelDto> dataStorageConnectors;

    List<@Valid DataStorageResultsJoinerDto> dataStorageResultsJoiners;

    @Builder.Default
    List<@Valid AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
