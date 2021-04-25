package pl.jalokim.crudwizard.test.utils.rest

import static org.apache.commons.lang3.StringUtils.EMPTY

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ErrorResponseMatcher extends TypeSafeMatcher<Map<String, Map<String, String>>> {

    private final String property
    private final Set<String> expectedErrors
    private final boolean onlyPropertyCheck
    private List<String> allErrorsForProperty

    private ErrorResponseMatcher(String property, boolean onlyPropertyCheck, Set<String> expectedErrors) {
        this.property = property
        this.expectedErrors = expectedErrors
        this.onlyPropertyCheck = onlyPropertyCheck
    }

    static ErrorResponseMatcher hasError(String property) {
        hasErrors(property, true, null,)
    }

    static ErrorResponseMatcher hasError(String property, String error) {
        hasErrors(property, false, error)
    }

    static ErrorResponseMatcher hasGlobalErrors(String error, String... errors) {
        hasErrors(EMPTY, false, error, errors)
    }

    static ErrorResponseMatcher hasGlobalError(String error) {
        hasGlobalErrors(error)
    }

    static ErrorResponseMatcher hasErrors(String property, boolean onlyPropertyCheck, String error, String... errors) {
        new ErrorResponseMatcher(property, onlyPropertyCheck, [error, *errors] as Set)
    }

    static List<Map> findErrorsDtoByProperty(Map jsonResponse, String property) {
        jsonResponse['errors'].findAll({it['property'] == property})
    }

    @Override
    protected boolean matchesSafely(Map<String, Map<String, String>> jsonResponse) {
        this.allErrorsForProperty = findErrorsDtoByProperty(jsonResponse, property)
            .collect {it['message']}
        if (onlyPropertyCheck) {
            return jsonResponse['errors'].any({it['property'] == property})
        }
        return allErrorsForProperty.containsAll(expectedErrors)
    }

    @Override
    protected void describeMismatchSafely(Map<String, Map<String, String>> item, Description description) {
        if (allErrorsForProperty.empty) {
            description.appendText('found none')
        } else {
            description.appendText('could not find: ')
                .appendValue(expectedErrors - allErrorsForProperty)
        }
    }

    @Override
    void describeTo(Description description) {
        description.appendText('Expected error(s) for property ')
            .appendValue(property)
            .appendText(': ')
            .appendValue(expectedErrors)
    }
}
