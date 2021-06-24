package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ValidateUnwrappedValue;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;

public class ContextFromPlaceholderArgs extends MessageInterpolatorContext {

    /**
     * Used for interpolate messages like in hibernate validation.
     */
    public ContextFromPlaceholderArgs(Map<String, Object> placeholderArgs) {
        super(new ConstraintDescriptorByPlaceholderArgs(placeholderArgs),
            null, null, placeholderArgs, placeholderArgs);
    }

    @RequiredArgsConstructor
    public static class ConstraintDescriptorByPlaceholderArgs implements ConstraintDescriptor {

        private final Map<String, Object> attributes;

        @Override
        public Annotation getAnnotation() {
            return null;
        }

        @Override
        public String getMessageTemplate() {
            return null;
        }

        @Override
        public Set<Class<?>> getGroups() {
            return null;
        }

        @Override
        public Set<Class<? extends Payload>> getPayload() {
            return null;
        }

        @Override
        public ConstraintTarget getValidationAppliesTo() {
            return null;
        }

        @Override
        public List<Class<? extends ConstraintValidator>> getConstraintValidatorClasses() {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Set<ConstraintDescriptor<?>> getComposingConstraints() {
            return null;
        }

        @Override
        public boolean isReportAsSingleViolation() {
            return false;
        }

        @Override
        public ValidateUnwrappedValue getValueUnwrapping() {
            return null;
        }

        @Override
        public Object unwrap(Class type) {
            return null;
        }
    }
}
