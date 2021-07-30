package pl.jalokim.crudwizard.genericapp.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;

@Value
@Builder(toBuilder = true)
public class GenericServiceArgument {

    Map<String, Object> httpQueryParams;
    /**
     * Translated to read classes based on class meta model by field 'queryArguments'
     * in EndpointMetaModel
     */
    Map<String, Object> httpQueryTranslated;
    Map<String, Object> requestBody;

    /**
     * Translated to read classes based on class meta model by field 'payloadMetamodel'
     * in EndpointMetaModel
     */
    Map<String, Object> requestBodyTranslated;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
    Map<String, Object> urlPathParams;
    EndpointMetaModel endpointMetaModel;
}
