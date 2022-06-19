package pl.jalokim.crudwizard.genericapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Value
@Builder(toBuilder = true)
public class GenericServiceArgument {

    Map<String, Object> httpQueryParams;
    /**
     * Translated to read classes based on class meta model by field 'queryArguments' in EndpointMetaModel
     */
    Map<String, Object> httpQueryTranslated;

    Pageable pageable;

    Sort sortBy;

    /**
     * Raw payload json
     */
    JsonNode requestBody;

    /**
     * Translated to real classes based on class meta model by field 'payloadMetamodel' in EndpointMetaModel
     */
    TranslatedPayload requestBodyTranslated;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
    Map<String, Object> urlPathParams;
    EndpointMetaModel endpointMetaModel;
    ValidationSessionContext validationContext;

    public static class GenericServiceArgumentBuilder {

        public GenericServiceArgumentBuilder pageable(Pageable pageable) {
            if (pageable != null && httpQueryParams != null) {
                if (httpQueryParams.containsKey("size") && httpQueryParams.containsKey("page")) {
                    this.pageable = pageable;
                }
                if (httpQueryParams.containsKey("sort")) {
                    this.sortBy = pageable.getSort();
                }
            }
            return this;
        }
    }
}
