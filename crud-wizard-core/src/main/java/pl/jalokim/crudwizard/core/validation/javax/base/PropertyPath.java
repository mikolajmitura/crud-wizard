package pl.jalokim.crudwizard.core.validation.javax.base;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@Getter
@Value
public class PropertyPath {

    List<PropertyPathPart> propertyParts;

    @Builder
    @Getter
    @Value
    public static class PropertyPathPart {
        String propertyName;
        Integer index;

        public boolean isListTypeProperty() {
            return index != null;
        }
    }

    public static class PropertyPathBuilder {

        List<PropertyPathPart> propertyParts = new ArrayList<>();

        public PropertyPathBuilder copy() {
            PropertyPathBuilder propertyPathBuilder = new PropertyPathBuilder();
            propertyPathBuilder.propertyParts.addAll(propertyParts);
            return propertyPathBuilder;
        }

        /**
         * Add name of next node property.
         */
        public PropertyPathBuilder addNextProperty(String propertyName) {
            propertyParts.add(PropertyPathPart.builder()
                .propertyName(propertyName)
                .build());
            return this;
        }

        /**
         * Add name of next node property with index of it.
         * @param propertyName name of property with list type
         * @param index index of element in list
         * @return PropertyPathBuilder
         */
        public PropertyPathBuilder addNextPropertyAndIndex(String propertyName, Integer index) {
            propertyParts.add(PropertyPathPart.builder()
                .propertyName(propertyName)
                .index(index)
                .build());
            return this;
        }
    }
}
