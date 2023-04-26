package pl.jalokim.crudwizard.core.validation.javax.base;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import pl.jalokim.utils.collection.CollectionUtils;

@Builder
@Getter
@Value
public class PropertyPath {

    List<PropertyPathPart> propertyParts;

    public String asFullPath() {
        StringBuilder result = new StringBuilder();

        elements(propertyParts)
            .forEachWithIndexed(indexed -> {
                PropertyPathPart propertyPart = indexed.getValue();

                String propertyName = propertyPart.getPropertyName();
                if (propertyName != null) {
                    if (!indexed.isFirst()) {
                        result.append(".");
                    }
                    result.append(propertyName);
                }

                var index = propertyPart.getIndex();
                if (index != null) {
                    result.append("[")
                        .append(index)
                        .append("]");
                }
            });

        return result.toString();
    }

    @Builder(toBuilder = true)
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
         *
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

        public PropertyPathBuilder addNextIndex(Integer index) {
            PropertyPathBuilder newBuilder = copy();
            List<PropertyPathPart> collectionToUpdate = newBuilder.propertyParts;
            PropertyPathPart propertyPathPart = CollectionUtils.getLastOrNull(collectionToUpdate);
            if (propertyPathPart != null && propertyPathPart.getIndex() == null) {
                collectionToUpdate.set(CollectionUtils.getLastIndex(collectionToUpdate),
                    propertyPathPart.toBuilder()
                        .index(index).build());
            } else {
                collectionToUpdate.add(PropertyPathPart.builder()
                    .index(index)
                    .build());
            }
            return newBuilder;
        }
    }
}
