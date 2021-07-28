package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.PathParamsAndUrlVariablesTheSame;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldShouldWhenOther(field = "payloadMetamodel", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"POST", "PUT", "PATCH"})
@FieldShouldWhenOther(field = "responseMetaModel", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"GET", "POST"})
@FieldShouldWhenOther(field = "payloadMetamodel", should = NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"GET", "DELETE"})
@FieldShouldWhenOther(field = "pathParams", should = NOT_NULL, whenField = EndpointMetaModelDto.HTTP_METHOD,
    is = EQUAL_TO_ANY, otherFieldValues = {"PUT", "PATCH"})
@PathParamsAndUrlVariablesTheSame
@EndpointNotExistsAlready
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
    String operationName;

    @Valid
    ClassMetaModelDto payloadMetamodel;

    @Valid
    ClassMetaModelDto queryArguments;

    @Valid
    ClassMetaModelDto pathParams;

    @Valid
    ServiceMetaModelDto serviceMetaModel;

    @Valid
    EndpointResponseMetaModelDto responseMetaModel;

    List<@Valid DataStorageConnectorMetaModelDto> dataStorageConnectors;
}
