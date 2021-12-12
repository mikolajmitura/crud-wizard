package pl.jalokim.crudwizard.core.utils;

import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringCaseUtils {

    public static String asUnderscoreLowercase(String text) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, text);
    }

    public static String asLowerCamel(String text) {
        if (text.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, text);
        }
        return text;
    }
}
