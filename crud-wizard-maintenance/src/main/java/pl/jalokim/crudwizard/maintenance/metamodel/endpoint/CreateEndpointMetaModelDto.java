package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.maintenance.metamodel.apitag.ApiTagDto;

@EqualsAndHashCode(callSuper = true)
@Value
public class CreateEndpointMetaModelDto extends ParentMetaModel {

    @NotNull(groups = UpdateContext.class)
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
}
