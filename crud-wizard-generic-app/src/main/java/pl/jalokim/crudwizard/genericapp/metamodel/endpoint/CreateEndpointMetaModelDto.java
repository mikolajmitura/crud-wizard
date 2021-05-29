package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class CreateEndpointMetaModelDto extends AdditionalPropertyMetaModel {

    @NotNull(groups = UpdateEndpointContext.class)
    Long id;

    @NotNull
    ApiTagDto apiTag;

    @NotNull
    String baseUrl;

    @NotNull
    HttpMethod httpMethod;

    @NotNull
    String operationName;

    ClassMetaModel payloadMetamodel;
    List<ClassMetaModel> queryArguments;

    ServiceMetaModel serviceMetaModel;

    EndpointResponseMetaModel responseMetaModel;

    // TODO maybe here which headers distinct to this endpoint
}
