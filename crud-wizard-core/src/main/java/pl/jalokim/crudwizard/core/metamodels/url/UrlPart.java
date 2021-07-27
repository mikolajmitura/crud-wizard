package pl.jalokim.crudwizard.core.metamodels.url;

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
}
