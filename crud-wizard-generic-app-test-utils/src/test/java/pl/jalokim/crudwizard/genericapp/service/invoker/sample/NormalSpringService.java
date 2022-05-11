package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;
import pl.jalokim.crudwizard.core.sample.SomeDocumentDto;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

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

    public void returnVoid(@Validated @RequestBody SamplePersonDto samplePersonDto) {

    }

    public Long validationContextAsArg(@RequestBody SamplePersonDto samplePersonDto, ValidationSessionContext validationSessionContext) {
        if (samplePersonDto.getId() == null && samplePersonDto.getSurname() == null) {
            validationSessionContext.addNextMessage("_id_surname", "NormalSpringService.invalid.id");
        }
        return 10L;
    }

    public String returnString() {
        return "StringValue";
    }

    public Integer returnInteger() {
        return 998;
    }


    public ResponseEntity<Boolean> returnResponseEntityBoolean(@RequestHeader String cookie) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(false);
    }

    public SamplePersonDto createSamplePersonDtoWithValidated(@RequestBody @Validated SamplePersonDto samplePersonDto) {
        return SamplePersonDto.builder()
            .id(1L)
            .name(samplePersonDto.getName())
            .surname(samplePersonDto.getSurname())
            .build();
    }

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

    public String methodWithInvalidJavaBean(@RequestBody InvalidJavaBean invalidJavaBean) {
        return "invoked";
    }

    public void missingReqRequestHeader(@RequestHeader String someRequiredHeader) {

    }

    public void missingReqRequestAllHeaders(@RequestHeader Map<String, String> allHeaders) {

    }

    public void missingReqRequestParam(@RequestParam String someRequiredParam) {

    }

    public void missingReqRequestBody(@RequestBody String rawJson) {

    }

    public void missingReqPathVariable(@PathVariable String someRequiredVariable) {

    }

    public CollectionElement[] getCollectionElementArray(String someString, String otherString) {
        return new CollectionElement[0];
    }

    public Map<String, Object> returnTranslatedHttpQuery(@RequestParam Map<String, Object> httpQueryTranslated) {
        return httpQueryTranslated;
    }

    public Long someMethodName(String stringArg, Long longArg) {
        return 1L;
    }

    public String someMethodName(String stringArg1, String stringArg2) {
        return stringArg2;
    }

    public String getSomeString() {
        return "someString";
    }

    public SomeDocumentDto getSomeDocumentDto() {
        return new SomeDocumentDto(null, 1L);
    }

    public SomeDocumentDto getSomeDocumentDtoById(Long id) {
        return new SomeDocumentDto(null, id);
    }

    public SomeDocumentDto getSomeDocumentDtoById(Long id, String someText, String someText2) {
        return new SomeDocumentDto(null, id);
    }

    public static class InvalidJavaBean {

        public String field1;
        public String field2;

        public InvalidJavaBean(String field1) {
            this.field1 = field1;
        }

        public InvalidJavaBean(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }
}
