package pl.jalokim.crudwizard.genericapp.mapper.generete;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GeneratedLineUtils {

    public static String wrapWithNextLineWith3Tabs(String textToFormat, Object... args) {
        return String.format("%n\t\t\t" + textToFormat, args);
    }

    public static String wrapWithNextLineWith2Tabs(String textToFormat, Object... args) {
        return String.format("%n\t\t" + textToFormat, args);
    }

    public static String wrapValueWithReturnStatement(String javaGenericTypeInfo, String variableName) {
        return String.format("return (%s) %s", javaGenericTypeInfo, variableName);
    }
}
