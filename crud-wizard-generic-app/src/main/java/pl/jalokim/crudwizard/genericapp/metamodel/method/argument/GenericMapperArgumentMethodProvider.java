package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;

@Getter
public class GenericMapperArgumentMethodProvider extends GenericMethodArgumentProvider {

    private final GenericMapperArgument genericMapperArgument;

    public GenericMapperArgumentMethodProvider(EndpointMetaModel endpointMetaModel, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, TranslatedPayload translatedPayload,
        JsonNode requestBodyAsJsonNode, Map<String, String> headers,
        Map<String, Object> httpQueryTranslated, Map<String, Object> urlPathParams,
        GenericMapperArgument genericMapperArgument) {
        super(endpointMetaModel, httpServletRequest, httpServletResponse, translatedPayload, requestBodyAsJsonNode,
            headers, httpQueryTranslated, urlPathParams);
        this.genericMapperArgument = genericMapperArgument;
    }


    public Object getMapperInput() {
        return genericMapperArgument.getSourceObject();
    }
}
