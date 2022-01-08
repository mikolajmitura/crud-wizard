package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.collection.Elements.bySplitText;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValueExtractorFromPath {

    @SuppressWarnings("unchecked")
    public static <T> T getValueFromPath(Object object, String pathToRead) {
        if (object == null) {
            return null;
        }
        Object currentObject = object;
        for (String pathPart : bySplitText(pathToRead, "\\.").asList()) {
            Object fieldValue = DataFieldsHelper.getFieldValue(currentObject, pathPart.replaceFirst("\\?", ""));
            if (fieldValue == null) {
                return null;
            }
            currentObject = fieldValue;
        }
        return (T) currentObject;
    }
}
