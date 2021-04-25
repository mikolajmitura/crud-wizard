package pl.jalokim.crudwizard.core.validation.javax.base;

import java.util.List;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.ContainerElementNodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
class CurrentValidationContextWrapper {

    ConstraintViolationBuilder constraintViolationBuilder;
    NodeBuilderCustomizableContext nodeBuilderCustomizableContext;
    ContainerElementNodeBuilderDefinedContext containerElementNodeBuilderDefinedContext;

    CurrentValidationContextWrapper addPropertyNode(String propertyName) {
        if (constraintViolationBuilder != null) {
            return CurrentValidationContextWrapper.builder()
                .nodeBuilderCustomizableContext(constraintViolationBuilder.addPropertyNode(propertyName))
                .build();
        }
        if (nodeBuilderCustomizableContext != null) {
            return CurrentValidationContextWrapper.builder()
                .nodeBuilderCustomizableContext(nodeBuilderCustomizableContext.addPropertyNode(propertyName))
                .build();
        }
        if (containerElementNodeBuilderDefinedContext != null) {
            return CurrentValidationContextWrapper.builder()
                .nodeBuilderCustomizableContext(containerElementNodeBuilderDefinedContext.addPropertyNode(propertyName))
                .build();
        }
        throw new IllegalStateException("CurrentPathHelper invalid");
    }

    CurrentValidationContextWrapper addComposedPropertyNode(String propertyName, Integer index) {
        if (constraintViolationBuilder != null) {
            return CurrentValidationContextWrapper.builder()
                .containerElementNodeBuilderDefinedContext(
                    constraintViolationBuilder.addContainerElementNode(propertyName, List.class, 0)
                        .inIterable()
                        .atIndex(index))
                .build();
        }
        if (nodeBuilderCustomizableContext != null) {
            return CurrentValidationContextWrapper.builder()
                .containerElementNodeBuilderDefinedContext(
                    nodeBuilderCustomizableContext.addContainerElementNode(propertyName, List.class, 0)
                        .inIterable()
                        .atIndex(index))
                .build();
        }
        if (containerElementNodeBuilderDefinedContext != null) {
            return CurrentValidationContextWrapper.builder()
                .containerElementNodeBuilderDefinedContext(
                    containerElementNodeBuilderDefinedContext.addContainerElementNode(propertyName, List.class, 0)
                        .inIterable()
                        .atIndex(index))
                .build();
        }
        throw new IllegalStateException("CurrentPathHelper invalid");
    }

    public void addConstraintViolation() {
        if (constraintViolationBuilder != null) {
            constraintViolationBuilder.addConstraintViolation();
        }
        if (nodeBuilderCustomizableContext != null) {
            nodeBuilderCustomizableContext.addConstraintViolation();
        }
        if (containerElementNodeBuilderDefinedContext != null) {
            containerElementNodeBuilderDefinedContext.addConstraintViolation();
        }
    }
}
