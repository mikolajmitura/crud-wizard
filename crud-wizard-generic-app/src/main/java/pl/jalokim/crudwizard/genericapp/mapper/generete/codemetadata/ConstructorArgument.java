package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;

@Value
@Builder
public class ConstructorArgument {

    List<String> annotations;
    Class<?> argumentType;
    String argumentName;

    public static ConstructorArgument newConstructorArgument(Class<?> argumentType) {
        return ConstructorArgument.builder()
            .argumentType(argumentType)
            .argumentName(StringCaseUtils.firstLetterToLowerCase(argumentType.getSimpleName()))
            .build();
    }

    public String getArgumentTypeAsText() {
        return argumentType.getCanonicalName();
    }
}
