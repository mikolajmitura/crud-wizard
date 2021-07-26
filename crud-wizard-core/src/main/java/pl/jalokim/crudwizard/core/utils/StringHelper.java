package pl.jalokim.crudwizard.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringHelper {

    public static String replaceAllWithEmpty(String textToClear, String... allToClear) {
        String currentText = textToClear;
        for (String toRemove : allToClear) {
            currentText = currentText.replace(toRemove, "");
        }
        return currentText;
    }
}
