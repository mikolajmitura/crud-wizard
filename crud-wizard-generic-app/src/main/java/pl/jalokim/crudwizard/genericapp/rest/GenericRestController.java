package pl.jalokim.crudwizard.genericapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceDelegator;

@RestController
@RequestMapping("/**/*")
@RequiredArgsConstructor
@Api(tags = "generic")
public class GenericRestController {

    private final GenericServiceDelegator genericServiceDelegator;

    @RequestMapping
    @ApiOperation("generic endpoint method for handle other endpoints")
    public ResponseEntity<Object> invokeHttpMethod(@RequestBody(required = false) JsonNode requestBody,
        @RequestParam(required = false) Map<String, Object> httpQueryParams,
        @RequestHeader(required = false) Map<String, String> headers,
        HttpServletRequest request, HttpServletResponse response, Pageable pageable) {

        return genericServiceDelegator.findAndInvokeHttpMethod(
            GenericServiceArgument.builder()
                .requestBody(requestBody)
                .httpQueryParams(httpQueryParams)
                .pageable(pageable)
                .headers(headers)
                .request(request)
                .response(response)
                .build()
        );
    }
}
