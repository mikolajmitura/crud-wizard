package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.ObjectWithVersion;
import pl.jalokim.utils.collection.Elements;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Data
public class WithAdditionalPropertiesMetaModel extends ObjectWithVersion {

    @Builder.Default
    private List<AdditionalPropertyMetaModel> additionalProperties = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unchecked")
    public <T> T getPropertyRealValue(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .map(property -> (T) property.getRealValue())
            .findFirst()
            .orElse(null);
    }

    public AdditionalPropertyMetaModel getProperty(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
    }

    public <T> T addProperty(String propertyName, Object value) {
        getAdditionalProperties().add(AdditionalPropertyMetaModel.builder()
            .name(propertyName)
            .valueAsObject(value)
            .valueRealClassName(getFullClassName(value))
            .build()
        );
        return (T) this;
    }

    public <T> T addAllAdditionalProperties(List<AdditionalPropertyMetaModel> additionalProperties) {
        this.getAdditionalProperties().addAll(additionalProperties);
        return (T) this;
    }

    public <T> T updateProperty(String propertyName, Object value) {
        for (int index = 0; index < getAdditionalProperties().size(); index++) {
            AdditionalPropertyMetaModel additionalPropertyDto = getAdditionalProperties().get(index);
            if (additionalPropertyDto.getName().equals(propertyName)) {
                getAdditionalProperties().remove(index);
                break;
            }
        }
        return addProperty(propertyName, value);
    }
}
