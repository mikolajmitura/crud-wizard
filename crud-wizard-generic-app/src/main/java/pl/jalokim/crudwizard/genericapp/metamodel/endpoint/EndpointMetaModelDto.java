package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointMetaModelDto extends AdditionalPropertyMetaModel {

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
    // TODO all models and DataStorage move to core module???
}
