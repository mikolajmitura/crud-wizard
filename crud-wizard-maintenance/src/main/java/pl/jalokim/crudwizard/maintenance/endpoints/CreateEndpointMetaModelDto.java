package pl.jalokim.crudwizard.maintenance.endpoints;

import java.util.List;
import lombok.Value;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;

@Value
public class CreateEndpointMetaModelDto {

    Long endpointId;
    String apiTag;
    String baseUrl;
    HttpMethod httpMethod;

    String operationName;

    ClassMetaModel payloadMetamodel;
    List<ClassMetaModel> queryArguments;
    ServiceMetaModel serviceMetaModel;

    EndpointResponseMetaModel responseMetaModel;
}
