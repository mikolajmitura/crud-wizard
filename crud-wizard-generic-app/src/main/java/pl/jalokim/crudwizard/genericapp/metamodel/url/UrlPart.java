package pl.jalokim.crudwizard.genericapp.metamodel.url;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UrlPart {

    String originalValue;
    String variableName;

    public boolean isPathVariable() {
        return variableName != null;
    }

    @Override
    public String toString() {
        return originalValue;
    }

    public static UrlPart normalUrlPart(String normalPathPart) {
        return new UrlPart(normalPathPart, null);
    }

    public static UrlPart variableUrlPart(String variableName) {
        return new UrlPart(String.format("{%s}", variableName), variableName);
    }
}
