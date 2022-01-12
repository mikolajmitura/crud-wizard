package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.ObjectWithVersionDto;
import pl.jalokim.utils.collection.Elements;

@EqualsAndHashCode(callSuper = true)
// TODO validator for check property name uniqueness
@SuppressWarnings("unchecked")
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public abstract class WithAdditionalPropertiesDto extends ObjectWithVersionDto {

    @Builder.Default
    private List<@Valid AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    public AdditionalPropertyDto getProperty(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
    }

    public <T> T addProperty(String propertyName, Object value) {
        getAdditionalProperties().add(AdditionalPropertyDto.builder()
            .name(propertyName)
            .valueRealClassName(getFullClassName(value))
            .rawJson(ObjectMapperConfig.objectToRawJson(value))
            .build()
        );
        return (T) this;
    }

    public <T> T updateProperty(String propertyName, Object value) {
        for (int index = 0; index < getAdditionalProperties().size(); index++) {
            AdditionalPropertyDto additionalPropertyDto = getAdditionalProperties().get(index);
            if (additionalPropertyDto.getName().equals(propertyName)) {
                getAdditionalProperties().remove(index);
                break;
            }
        }
        return addProperty(propertyName, value);
    }
}
