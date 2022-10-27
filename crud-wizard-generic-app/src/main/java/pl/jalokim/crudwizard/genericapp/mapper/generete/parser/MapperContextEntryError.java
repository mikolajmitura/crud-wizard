package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MapperContextEntryError {

    Integer columnNumber;

    String errorReason;

    public String toString() {
        return getMessageWithoutEntryIndex();
    }

    public String getMessageWithoutEntryIndex() {
        String columnPart = columnNumber == null ? "" : createMessagePlaceholder("MapperContextEntryError.column") + ":" + columnNumber + ", ";
        return String.format("%s%s", columnPart, errorReason);
    }
}
