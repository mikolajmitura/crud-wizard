package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.concatElements;
import static pl.jalokim.utils.string.StringUtils.tabsNTimes;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumEntryMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.TranslationArgs;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.collection.CollectionUtils;

@Service
@RequiredArgsConstructor
public class NormalSpringService {

    private final MetaModelContextService metaModelContextService;

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

    @Validated
    public SamplePersonDto createSamplePersonDtoWithValidated(@RequestBody @Validated SamplePersonDto samplePersonDto) {
        return SamplePersonDto.builder()
            .id(1L)
            .name(samplePersonDto.getName())
            .surname(samplePersonDto.getSurname())
            .build();
    }

    @Validated
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

    public Long testNotSupportedLocale(@RequestBody Map<String, Object> payload) {
        AppMessageSourceHolder.getMessage("not-supported-translation-key");
        return 100L;
    }

    public String cannotFindTranslationKey() {
        return AppMessageSourceHolder.getMessage("cannot.find.translation.key");
    }

    public String getMessageWithArgs(@RequestBody TranslationArgs translationArgs) {
        List<Object> argumentsByIndexes = translationArgs.getArgumentsByIndexes();
        if (CollectionUtils.isNotEmpty(argumentsByIndexes)) {
            Object[] args = elements(argumentsByIndexes).asArray(new Object[0]);
            return AppMessageSourceHolder.getMessage(
                translationArgs.getPlaceholder(), args);
        } else if (translationArgs.getArgumentsByName() != null) {
            return AppMessageSourceHolder.getMessage(
                translationArgs.getPlaceholder(),
                translationArgs.getArgumentsByName());
        }
        return AppMessageSourceHolder.getMessage(translationArgs.getPlaceholder());
    }

    public CollectionElement[] getCollectionElementArray(String someString, String otherString) {
        return new CollectionElement[]{
            new CollectionElement(someString, otherString, null, null)
        };
    }

    public Map<String, Object> returnTranslatedHttpQuery(@RequestParam Map<String, Object> httpQueryTranslated) {
        return httpQueryTranslated;
    }

    public Long someMethodName(String stringArg, Long longArg) {
        return (long) stringArg.length();
    }

    public String someMethodName(String stringArg1, String stringArg2) {
        return stringArg2;
    }

    public String getSomeString() {
        return "someStringValue";
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

    public String getTranslatedInfo(@RequestBody Map<String, Object> personData) {
        ClassMetaModel person = metaModelContextService.getClassMetaModelByName("person");
        String header = getClassMetaModelTranslated(person);

        List<String> fieldLines = new ArrayList<>();
        addObjectLines(personData, person, fieldLines, 1);

        return concatElements("\n", header,
            elements(fieldLines).asConcatText("\n"));
    }

    private void addObjectLines(Map<String, Object> values,
        ClassMetaModel classMetaModel, List<String> fieldLines, int indentation) {
        for (FieldMetaModel field : classMetaModel.getFields()) {
            Object value = values.get(field.getFieldName());
            if (value instanceof Map) {
                fieldLines.add(tabsNTimes(indentation) +
                    getClassMetaModelTranslated(field.getFieldType()) + ":" +
                    getFieldTranslated(field) + "=");
                addObjectLines((Map<String, Object>) value,
                    field.getFieldType(), fieldLines, indentation + 1);
            } else {

                if (field.getFieldName().equals("someColor") ||
                    field.getFieldName().equals("someEnum")) {
                    EnumEntryMetaModel enumByName = field.getFieldType().getEnumMetaModel()
                        .getEnumByName(value.toString());
                    value = enumByName.getTranslated() + "(" + enumByName.getTranslation().getTranslationKey() + ")";
                }

                fieldLines.add(tabsNTimes(indentation) +
                    getClassMetaModelTranslated(field.getFieldType()) + ":" +
                    getFieldTranslated(field) + "=" + value);
            }
        }
    }

    private String getClassMetaModelTranslated(ClassMetaModel classMetaModel) {
        return classMetaModel.getTranslatedName()
         + (classMetaModel.getTranslationName() == null ? "" : "(" + classMetaModel.getTranslationName().getTranslationKey() + ")");
    }

    private String getFieldTranslated(FieldMetaModel fieldMetaModel) {
        return fieldMetaModel.getTranslatedName() + "(" + fieldMetaModel.getTranslationFieldName().getTranslationKey() + ")";
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
