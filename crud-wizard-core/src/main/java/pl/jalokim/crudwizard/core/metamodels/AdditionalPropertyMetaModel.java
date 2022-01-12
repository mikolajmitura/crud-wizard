package pl.jalokim.crudwizard.core.metamodels;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AdditionalPropertyMetaModel {

    Long id;

    String name;

    String valueRealClassName;

    String rawJson;

    Object valueAsObject;

    @SuppressWarnings("unchecked")
    public <T> T getRealValue() {
        return (T) valueAsObject;
    }
}
