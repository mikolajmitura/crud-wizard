package pl.jalokim.crudwizard.genericapp.mapper.generete.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Getter;
import pl.jalokim.crudwizard.core.exception.ApplicationException;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class MapperGenerationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final List<MessagePlaceholder> messagePlaceholders;

    public MapperGenerationException(List<MessagePlaceholder> messagePlaceholders) {
        super(createMessagePlaceholder("MapperGenerationException.message", getErrorsAsText(messagePlaceholders)));
        this.messagePlaceholders = messagePlaceholders;
    }

    private static String getErrorsAsText(List<MessagePlaceholder> messagePlaceholders) {
        return System.lineSeparator() + elements(messagePlaceholders)
            .map(MessagePlaceholder::translateMessage)
            .concatWithNewLines();
    }
}
