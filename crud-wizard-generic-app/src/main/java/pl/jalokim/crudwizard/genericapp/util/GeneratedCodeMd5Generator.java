package pl.jalokim.crudwizard.genericapp.util;

import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.springframework.util.DigestUtils;

@UtilityClass
public class GeneratedCodeMd5Generator {

    public static String generateMd5Hash(String simpleClassName, String sourceCode, Long sessionTimestamp) {
        String realClassName = simpleClassName.replace(sessionTimestamp.toString(), "");
        String realSourceCode = sourceCode.replace(simpleClassName, realClassName);
        return DigestUtils.md5DigestAsHex(realSourceCode.getBytes(StandardCharsets.UTF_8));
    }
}
