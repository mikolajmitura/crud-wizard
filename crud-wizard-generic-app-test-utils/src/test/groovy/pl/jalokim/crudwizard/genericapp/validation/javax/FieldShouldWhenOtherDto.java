package pl.jalokim.crudwizard.genericapp.validation.javax;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MAX;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MIN;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EQUAL_TO_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

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
    should = EMPTY,
    whenField = "someEnums",
    is = EMPTY)
@FieldShouldWhenOther(field = "emptyOrNull",
    should = EMPTY_OR_NULL,
    whenField = "emptyOrNullOther",
    is = EMPTY_OR_NULL)
@FieldShouldWhenOther(field = "shouldBeNotNull",
    should = NOT_NULL,
    whenField = "isEmptyList",
    is = EMPTY)
@FieldShouldWhenOther(field = "blankTestFiled",
    should = NOT_BLANK,
    whenField = "whenBlankTestFiled",
    is = NOT_BLANK)
@FieldShouldWhenOther(field = "someStringNotEmpty",
    should = NOT_EMPTY,
    whenField = "someMapNotEmpty",
    is = NOT_EMPTY)
@FieldShouldWhenOther(field = "someStringNotEmpty",
    should = NOT_EMPTY,
    whenField = "someCollectionNotEmpty",
    is = NOT_EMPTY)
@FieldShouldWhenOther(field = "someTextField",
    should = MAX,
    fieldValues = "2",
    whenField = "someListString",
    is = MIN,
    otherFieldValues = "3")
@FieldShouldWhenOther(field = "someMap",
    should = MAX,
    fieldValues = "1",
    whenField = "someLong",
    is = MIN,
    otherFieldValues = "10")
@FieldShouldWhenOther(field = "someMap",
    should = MIN,
    fieldValues = "2",
    whenField = "someDouble",
    is = MAX,
    otherFieldValues = "10.50")
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

    String blankTestFiled;
    String whenBlankTestFiled;

    Map<String, String> someMapNotEmpty;
    String someStringNotEmpty;
    List<String> someCollectionNotEmpty;

    // used for test validation of MIN, MAX
    String someTextField;
    List<String> someListString;
    Map<?, ?> someMap;
    Long someLong;
    Double someDouble;

    public enum SomeEnum {
        ENTRY_1, ENTRY_2, ENTRY_3
    }
}
