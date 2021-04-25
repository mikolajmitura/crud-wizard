package pl.jalokim.crudwizard.genericapp.validation.javax;

public enum OtherFieldMatch {

    /**
     * When is normal field and is equal to any on list.
     */
    EQUAL_TO_ANY,

    /**
     * when other field is collection
     */
    CONTAINS_ALL,

    /**
     * when other field is collection
     */
    CONTAINS_ANY,

    /**
     * When is normal field and is not equal to all
     */
    NOT_EQUAL_TO_ALL,

    NULL,

    NOT_NULL
}
