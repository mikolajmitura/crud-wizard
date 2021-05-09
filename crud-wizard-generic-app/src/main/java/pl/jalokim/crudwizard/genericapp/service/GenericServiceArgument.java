package pl.jalokim.crudwizard.genericapp.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GenericServiceArgument {

    Map<String, Object> httpQueryParams;
    Map<String, Object> requestBody;
    Map<String, String> headers;
    HttpServletRequest request;
    HttpServletResponse response;
}
