package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class MappingException extends TechnicalException {

    public MappingException(MessagePlaceholder messagePlaceHolder) {
        super(messagePlaceHolder);
    }
}
