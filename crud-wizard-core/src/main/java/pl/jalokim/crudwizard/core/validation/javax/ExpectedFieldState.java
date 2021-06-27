package pl.jalokim.crudwizard.core.validation.javax;

import java.util.List;

public enum ExpectedFieldState {

    /**
     * When is normal field and is equal to any on list.
     */
    EQUAL_TO_ANY,

    /**
     * when is Collection
     */
    CONTAINS_ALL,

    /**
     * when field is Collection
     */
    CONTAINS_ANY,

    /**
     * When is normal field and is not equal to all
     */
    NOT_EQUAL_TO_ALL,

    NULL,

    NOT_NULL,

    /**
     * For Collections, Map or String
     */
    EMPTY,

    /**
     * For Collections, Map or String
     */
    EMPTY_OR_NULL,

    /**
     * For String
     */
    NOT_BLANK,

    /**
     * For Collections, Map or String
     */
    NOT_EMPTY,

    /**
     * For Collections, Map or String as min size
     * For numbers min value
     */
    MIN,

    /**
     * For Collections, Map or String as max size
     * For numbers max value
     */
    MAX;

    public static final List<ExpectedFieldState> FOR_COLLECTIONS = List.of(CONTAINS_ALL, CONTAINS_ANY, EMPTY, EMPTY_OR_NULL, NOT_EMPTY);
    public static final List<ExpectedFieldState> FOR_STRING = List.of(NOT_BLANK);
    public static final List<ExpectedFieldState> FOR_STRING_AND_COLLECTION = List.of(EMPTY, EMPTY_OR_NULL, NOT_EMPTY);
    public static final List<ExpectedFieldState> FOR_STRING_AND_COLLECTION_AND_NUMBER = List.of(MIN, MAX);
    public static final List<ExpectedFieldState> WITHOUT_OTHER_FIELD_VALUES = List.of(EMPTY, EMPTY_OR_NULL, NOT_EMPTY, NOT_BLANK, NULL, NOT_NULL);
    public static final List<ExpectedFieldState> WITH_OTHER_FIELD_VALUES = List.of(EQUAL_TO_ANY, CONTAINS_ALL, CONTAINS_ANY, NOT_EQUAL_TO_ALL, MIN, MAX);
}
