package pl.jalokim.crudwizard.genericapp.validation.javax;

import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.CONTAINS_ALL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.CONTAINS_ANY;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EMPTY_COLLECTION;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EMPTY_COLLECTION_OR_NULL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NOT_EQUAL_TO_ALL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NOT_NULL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NULL;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@FieldShouldWhenOther(field = "personalNumber",
    should = NULL,
    whenField = "documentNumber",
    is = NOT_NULL)
@FieldShouldWhenOther(field = "realId",
    should = NOT_NULL,
    whenField = "version", is = NOT_NULL)
@FieldShouldWhenOther(field = "someEnum",
    should = EQUAL_TO_ANY,
    fieldValues = {"ENTRY_1", "ENTRY_2"},
    whenField = "whenSomeEnum",
    is = EQUAL_TO_ANY, otherFieldValues = "100")
@FieldShouldWhenOther(field = "someList",
    should = CONTAINS_ALL,
    fieldValues = {"12", "11", "13"},
    whenField = "someSet",
    is = CONTAINS_ALL,
    otherFieldValues = {"15", "12"})
@FieldShouldWhenOther(field = "someList",
    should = CONTAINS_ANY,
    fieldValues = {"100", "1000"},
    whenField = "someSet",
    is = CONTAINS_ANY,
    otherFieldValues = {"99", "999"})
@FieldShouldWhenOther(field = "someList",
    should = EQUAL_TO_ANY,
    fieldValues = "[text1, text2, text3]",
    whenField = "someEnums",
    is = EQUAL_TO_ANY,
    otherFieldValues = "[ENTRY_2, ENTRY_3]")
@FieldShouldWhenOther(field = "notEqualToAll",
    should = NOT_EQUAL_TO_ALL,
    fieldValues = {"text1", "text2"},
    whenField = "notEqualToAllOther",
    is = NOT_EQUAL_TO_ALL,
    otherFieldValues = {"text11", "text22"})
@FieldShouldWhenOther(field = "someSet",
    should = EMPTY_COLLECTION,
    whenField = "someEnums",
    is = EMPTY_COLLECTION)
@FieldShouldWhenOther(field = "emptyOrNull",
    should = EMPTY_COLLECTION_OR_NULL,
    whenField = "emptyOrNullOther",
    is = EMPTY_COLLECTION_OR_NULL)
@FieldShouldWhenOther(field = "shouldBeNotNull",
    should = NOT_NULL,
    whenField = "isEmptyList",
    is = EMPTY_COLLECTION)
@Builder
@Value
public class FieldShouldWhenOtherDto {

    String personalNumber;
    String documentNumber;

    Long realId;
    Long version;

    SomeEnum someEnum;
    Long whenSomeEnum;

    List<String> someList;
    Set<String> someSet;
    List<SomeEnum> someEnums;

    String notEqualToAll;
    String notEqualToAllOther;

    List<Long> emptyOrNull;
    List<Long> emptyOrNullOther;

    String shouldBeNotNull;
    List<String> isEmptyList;

    public enum SomeEnum {
        ENTRY_1, ENTRY_2, ENTRY_3
    }
}
