package pl.jalokim.crudwizard.genericapp.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;

@Value
@Builder(toBuilder = true)
public class GenericServiceArgument {

    RawEntityObject httpQueryParams;
    /**
     * Translated to read classes based on class meta model by field 'queryArguments'
     * in EndpointMetaModel
     */
    RawEntityObject httpQueryTranslated;
    RawEntityObject requestBody;

    /**
     * Translated to read classes based on class meta model by field 'payloadMetamodel'
     * in EndpointMetaModel
     */
    RawEntityObject requestBodyTranslated;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
    RawEntityObject urlPathParams;
    EndpointMetaModel endpointMetaModel;
}
