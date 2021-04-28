package pl.jalokim.crudwizard.genericapp.validation.javax;

import java.util.List;

public enum OtherFieldMatch {

    /**
     * When is normal field and is equal to any on list.
     */
    EQUAL_TO_ANY,

    /**
     * when is collection
     */
    CONTAINS_ALL,

    /**
     * when field is collection
     */
    CONTAINS_ANY,

    /**
     * When is normal field and is not equal to all
     */
    NOT_EQUAL_TO_ALL,

    NULL,

    NOT_NULL,

    /**
     * For collections
     */
    EMPTY_COLLECTION,

    /**
     * For collections
     */
    EMPTY_COLLECTION_OR_NULL,

    /**
     * For String
     */
    NOT_BLANK,

    /**
     * For String
     */
    BLANK;

    public static final List<OtherFieldMatch> FOR_COLLECTIONS = List.of(CONTAINS_ALL, CONTAINS_ANY, EMPTY_COLLECTION, EMPTY_COLLECTION_OR_NULL);
    public static final List<OtherFieldMatch> FOR_STRING = List.of(NOT_BLANK, BLANK);
}
