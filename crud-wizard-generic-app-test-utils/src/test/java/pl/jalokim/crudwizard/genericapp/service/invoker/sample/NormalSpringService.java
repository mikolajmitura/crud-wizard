package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;

@Service
public class NormalSpringService {

    public SamplePersonDto getSamplePersonDtoInvalid(@RequestBody @NotNull JsonNode jsonNode,
        @RequestBody @Validated SamplePersonDto samplePersonDto,
        String argWithoutAnnotation,
        @RequestHeader Map<String, Object> headers,
        @RequestHeader("cookie") @NotBlank String cookieValue) {
        return null;
    }

    public SamplePersonDto getSamplePersonDtoInvalid2(@RequestBody @NotNull JsonNode jsonNode,
        @Validated SamplePersonDto samplePersonDto, String argWithoutAnnotation) {
        return null;
    }

    // TODO #02 in IT check that @Validated @Valid @NotBlank invoked
    public Map<String, Object> returnAllInputs(@RequestBody @NotNull JsonNode jsonNode,
        @RequestBody @Validated SamplePersonDto samplePersonDto,
        @RequestBody TranslatedPayload jsonNodeTranslated,
        TranslatedPayload jsonNodeTranslated2,
        JsonNode jsonNode2,
        @RequestBody String rawJson,
        @RequestHeader Map<String, Object> headers,
        @RequestHeader("cookie") @NotBlank String cookieValue,
        @RequestParam("lastContact") String lastContactAsText,
        @PathVariable String objectId,
        @RequestParam("lastContact") LocalDate lastContact,
        @RequestParam("numberAsText") Long numberAsTextAsNumber,
        @RequestParam("numberAsText") String numberAsText,
        @PathVariable("objectId") Long objectIdAsLong,
        @PathVariable("objectId") String objectIdAsText,
        @PathVariable("objectUuid") String objectUuid,
        @PathVariable(value = "notExistPathVar", required = false) String notExistPathVar,
        @RequestParam(value = "notExistQueryParam", required = false) LocalDate notExistQueryParam,
        @RequestHeader(value = "contend-type", required = false) String contentType) {

        Map<String, Object> newMap = new HashMap<>();

        newMap.put("fromJsonNode", jsonNode.findValue("name").asText() + jsonNode.findValue("surname").asText());
        newMap.put("jsonNodeTranslated", jsonNodeTranslated);
        newMap.put("jsonNodeTranslated2", jsonNodeTranslated2);
        newMap.put("jsonNode2", jsonNode2);
        newMap.put("samplePersonDto", samplePersonDto);
        newMap.put("headers", headers);
        newMap.put("cookieValue", cookieValue);
        newMap.put("lastContactAsText", lastContactAsText);
        newMap.put("lastContact", lastContact);
        newMap.put("notExistQueryParam", notExistQueryParam);
        newMap.put("numberAsTextAsNumber", numberAsTextAsNumber);
        newMap.put("numberAsText", numberAsText);
        newMap.put("contentType", contentType);
        newMap.put("objectIdAsLong", objectIdAsLong);
        newMap.put("objectIdAsText", objectIdAsText);
        newMap.put("objectUuid", objectUuid);
        newMap.put("notExistPathVar", notExistPathVar);
        newMap.put("objectIdWithoutAnnotationParam", objectId);
        newMap.put("rawJson", rawJson);
        return newMap;
    }

    public String duplicatedMethodName(String value) {
        return null;
    }

    public String duplicatedMethodName(Double value) {
        return null;
    }
}
