package pl.jalokim.crudwizard.genericapp.rest;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.genericapp.service.GenericService;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;

@RestController
@RequestMapping("/**/*")
@RequiredArgsConstructor
public class GenericRestController {

    private final GenericService genericService;

    @RequestMapping
    public ResponseEntity<Object> invokeHttpMethod(@RequestBody(required = false) Map<String, Object> requestBody,
        @RequestParam Map<String, Object> params, @RequestHeader Map<String, String> headers,
        HttpServletRequest request, HttpServletResponse response) {

        return genericService.findAndInvokeHttpMethod(
            GenericServiceArgument.builder()
                .requestBody(requestBody)
                .params(params)
                .headers(headers)
                .request(request)
                .response(response)
                .build()
        );
    }
}
