package pl.jalokim.crudwizard.genericapp.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.RawEntity;

@Value
@Builder(toBuilder = true)
public class GenericServiceArgument {

    RawEntity httpQueryParams;
    RawEntity requestBody;

    /**
     * Translated to read classes based on class meta model.
     */
    RawEntity requestBodyTranslated;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
}
