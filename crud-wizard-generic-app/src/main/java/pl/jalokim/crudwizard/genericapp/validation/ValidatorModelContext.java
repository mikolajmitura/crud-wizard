package pl.jalokim.crudwizard.genericapp.validation;

import static pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders.newEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import pl.jalokim.crudwizard.core.exception.ErrorWithPlaceholders;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;
import pl.jalokim.utils.collection.CollectionUtils;

@Data
public class ValidatorModelContext {

    private final List<ErrorWithPlaceholders> currentEntries = new ArrayList<>();
    private final ValidatorMetaModel currentValidatorMetaModel;
    private final Map<String, Object> messagePlaceholders;
    private final PropertyPath currentPath;
    private final ClassMetaModel validatedClassMetaModel;
    private boolean validationPassed;

    public ValidatorModelContext(ValidatorMetaModel currentValidatorMetaModel, ClassMetaModel validatedClassMetaModel, PropertyPath currentPath) {
        this.currentValidatorMetaModel = currentValidatorMetaModel;
        this.messagePlaceholders = currentValidatorMetaModel.fetchMessagePlaceholders();
        this.currentPath = currentPath;
        this.validatedClassMetaModel = validatedClassMetaModel;
    }

    public boolean hasCustomMessages() {
        return CollectionUtils.isNotEmpty(currentEntries);
    }

    public void addEntry(String property, String translationKey, String message) {
        currentEntries.add(newEntry(property, translationKey, message));
    }
}
