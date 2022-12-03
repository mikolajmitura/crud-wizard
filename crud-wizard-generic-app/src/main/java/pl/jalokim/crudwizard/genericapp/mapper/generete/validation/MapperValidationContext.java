package pl.jalokim.crudwizard.genericapp.mapper.generete.validation;

import java.util.ArrayList;
import java.util.List;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class MapperValidationContext {

    public List<MessagePlaceholder> messagePlaceholders = new ArrayList<>();

    public void addError(MessagePlaceholder messagePlaceholder) {
        messagePlaceholders.add(messagePlaceholder);
    }

    public void checkValidationResults() {
        if (!messagePlaceholders.isEmpty()) {
            throw new MapperGenerationException(messagePlaceholders);
        }
    }
}
