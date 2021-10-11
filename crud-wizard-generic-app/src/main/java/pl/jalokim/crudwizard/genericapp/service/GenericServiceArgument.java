package pl.jalokim.crudwizard.genericapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Value
@Builder(toBuilder = true)
public class GenericServiceArgument {

    Map<String, Object> httpQueryParams;
    /**
     * Translated to read classes based on class meta model by field 'queryArguments'
     * in EndpointMetaModel
     */
    Map<String, Object> httpQueryTranslated;

    /**
     * Raw payload json
     */
    JsonNode requestBody;

    /**
     * Translated to real classes based on class meta model by field 'payloadMetamodel'
     * in EndpointMetaModel
     */
    TranslatedPayload requestBodyTranslated;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
    Map<String, Object> urlPathParams;
    EndpointMetaModel endpointMetaModel;
    ValidationSessionContext validationContext;
}
