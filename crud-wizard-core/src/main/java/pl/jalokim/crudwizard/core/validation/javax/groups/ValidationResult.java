package pl.jalokim.crudwizard.core.validation.javax.groups;

import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidationResult {

    private ConstraintViolationException constraintViolationException;

    public boolean hasErrors() {
        return constraintViolationException != null;
    }
}
