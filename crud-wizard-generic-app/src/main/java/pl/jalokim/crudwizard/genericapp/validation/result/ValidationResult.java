package pl.jalokim.crudwizard.genericapp.validation.result;

import static pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders.newEntry;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders;

@Data
public class ValidationResult {

    public String message;
    public List<ErrorWithPlaceholders> entries = new ArrayList<>();

    public void addEntry(String property, String translationKey, String message) {
        entries.add(newEntry(property, translationKey, message));
    }

    public void addEntry(ErrorWithPlaceholders errorEntry) {
        entries.add(errorEntry);
    }
}
