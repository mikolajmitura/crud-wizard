package pl.jalokim.crudwizard.genericapp.util;

public class StringHelper {

    public static String replaceAllWithEmpty(String textToClear, String... allToClear) {
        String currentText = textToClear;
        for (String toRemove : allToClear) {
            currentText = currentText.replace(toRemove, "");
        }
        return currentText;
    }
}
