package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.utils.collection.Elements.bySplitText;

import pl.jalokim.crudwizard.core.utils.DataFieldsHelper;

public class ValueExtractorFromPath {

    public static Object getValueFromPath(Object object, String pathToRead) {
        Object currentObject = object;
        for (String pathPart : bySplitText(pathToRead, "\\.").asList()) {
            Object fieldValue = DataFieldsHelper.getFieldValue(currentObject, pathPart);
            if (fieldValue == null) {
                return null;
            }
            currentObject = fieldValue;
        }
        return currentObject;
    }
}
