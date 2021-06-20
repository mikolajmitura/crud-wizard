package pl.jalokim.crudwizard.core.metamodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AdditionalPropertyDto {

    Long id;
    String name;
    String valueRealClassName;
    Object value;

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T> T getRealValue() {
        return (T) value;
    }
}
