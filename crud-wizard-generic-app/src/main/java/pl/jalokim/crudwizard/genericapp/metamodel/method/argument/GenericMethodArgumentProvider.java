package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;

@RequiredArgsConstructor
@Getter
public abstract class GenericMethodArgumentProvider {

    private final EndpointMetaModel endpointMetaModel;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final TranslatedPayload translatedPayload;
    private final JsonNode requestBodyAsJsonNode;
    private final Map<String, String> headers;
    private final Map<String, Object> httpQueryTranslated;
    private final Map<String, Object> urlPathParams;
}
