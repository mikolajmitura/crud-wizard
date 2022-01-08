package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.rawJsonToObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class AdditionalPropertyDto {

    Long id;
    @NotNull
    String name;
    @ClassExists
    String valueRealClassName;
    String rawJson;
    @JsonIgnore
    Object valueAsObject;

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T> T getRealValue() {
        if (valueAsObject == null && rawJson != null) {
            valueAsObject = rawJsonToObject(rawJson, valueRealClassName);
        }
        return (T) valueAsObject;
    }
}
