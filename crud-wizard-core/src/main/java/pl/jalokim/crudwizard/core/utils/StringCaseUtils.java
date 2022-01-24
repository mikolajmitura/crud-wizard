package pl.jalokim.crudwizard.core.utils;

import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.string.StringUtils;

@UtilityClass
public class StringCaseUtils {

    // TODO move to java-utils
    public static String asUnderscoreLowercase(String text) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, text);
    }

    // TODO move to java-utils
    public static String asLowerCamel(String text) {
        if (text.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, text);
        }
        return text;
    }

    // TODO move to java-utils
    public static String firstLetterToUpperCase(String text) {
        if (StringUtils.isNotBlank(text)) {
            char firstLetter = text.charAt(0);
            if (Character.isLowerCase(firstLetter)) {
                return Character.toUpperCase(firstLetter) + text.substring(1);
            }
        }
        return text;
    }

    // TODO move to java-utils
    public static String firstLetterToLowerCase(String text) {
        if (StringUtils.isNotBlank(text)) {
            char firstLetter = text.charAt(0);
            if (Character.isUpperCase(firstLetter)) {
                return Character.toLowerCase(firstLetter) + text.substring(1);
            }
        }
        return text;
    }
}
