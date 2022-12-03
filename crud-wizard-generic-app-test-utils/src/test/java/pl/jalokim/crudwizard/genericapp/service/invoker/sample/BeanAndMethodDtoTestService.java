package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;

@Service
public class BeanAndMethodDtoTestService {

    public Map<String, Object> method1(@RequestBody Map<String, Long> arg1,
        @RequestHeader Object headers,
        @RequestHeader Map<String, Object> headers1,
        @RequestHeader Map<String, Long> headers2,
        @RequestHeader("x-value") Object headersXValue,
        @RequestParam("name") Object requestParamName,
        @RequestParam Object RequestParam,
        @PathVariable Object pathVars,
        EndpointMetaModel endpointMetaModel,
        HttpServletRequest httpServletRequest
    ) {
        return null;
    }

    public Long method2(EndpointMetaModel endpointMetaModel,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        JsonNode jsonNode,
        TranslatedPayload translatedPayload,
        @RequestBody Map<String, Object> arg1,
        @RequestHeader Map<String, String> headers1,
        @RequestHeader("x-value") Long headersXValue1,
        @RequestHeader(name = "x-value") String headersXValue2,
        @RequestParam("name") String requestParamName,
        @PathVariable("pathVariable") String pathVariable
    ) {
        return null;
    }

    public Map<String, Object> invalidMethod3(@RequestBody Long longArgument) {
        return null;
    }

    public String validMethod3(@RequestBody List<Map<String, Object>> arg1,
        JsonNode jsonNode,
        @RequestBody List<SomeRawDto> arg2) {
        return null;
    }

    ResponseEntity<Long> invalidMethod4(Long input, @RequestBody Long input3,
        @RequestBody Map<String, Object> mapArg) {
        return null;
    }

    ResponseEntity<String> validMethod4(@RequestBody SomeRawDto someRawDto) {
        return null;
    }

    Long invalidMethod5(@RequestBody Map<String, Object> mapArg,
        @RequestBody List<Map<String, Object>> mapArgList) {
        return null;
    }

    String validMethod5(@RequestBody List<SomeRawDto> mapArgList) {
        return null;
    }

    String invalidMethod6(@RequestBody SomeRawDto someRawDto) {
        return null;
    }

    Long validMethod6(@RequestBody Long asLong,
        @RequestBody Integer asInteger,
        @RequestBody String asString) {
        return null;
    }

    String invalidMethod7(@RequestBody SomeRawDto rawDto) {
        return null;
    }

    ResponseEntity<LocalDateTime> validMethod7(@RequestBody String enumAsText) {
        return null;
    }

    Long validMethod8(
        @RequestBody String enumAsText,
        @RequestBody SomeEnum1 someEnum1) {
        return null;
    }

    public String invalidMethod9(@RequestBody SomeRawDto someRawDto) {
        return null;
    }

    public Long validMethod9(@RequestBody Map<String, Long> someMap) {
        return null;
    }
}
