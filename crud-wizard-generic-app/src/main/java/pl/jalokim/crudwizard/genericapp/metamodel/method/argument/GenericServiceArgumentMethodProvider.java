package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Getter
public class GenericServiceArgumentMethodProvider extends GenericMethodArgumentProvider {

    private final GenericServiceArgument genericServiceArgument;
    private final ValidationSessionContext validationSessionContext;
    private final TranslatedPayload translatedPayload;

    public GenericServiceArgumentMethodProvider(EndpointMetaModel endpointMetaModel, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, TranslatedPayload translatedPayload, JsonNode requestBodyAsJsonNode,
        Map<String, String> headers, Map<String, Object> httpQueryTranslated, Map<String, Object> urlPathParams,
        GenericServiceArgument genericServiceArgument, ValidationSessionContext validationSessionContext,
        TranslatedPayload translatedPayload1) {
        super(endpointMetaModel, httpServletRequest, httpServletResponse,
            translatedPayload, requestBodyAsJsonNode, headers, httpQueryTranslated, urlPathParams);
        this.genericServiceArgument = genericServiceArgument;
        this.validationSessionContext = validationSessionContext;
        this.translatedPayload = translatedPayload1;
    }
}
