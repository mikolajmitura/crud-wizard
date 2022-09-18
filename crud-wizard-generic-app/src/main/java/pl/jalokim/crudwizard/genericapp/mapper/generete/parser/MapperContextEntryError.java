package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MapperContextEntryError {

    int entryIndex;
    Integer columnNumber;

    String errorReason;

    public String toString() {
        return getMessageWithEntryIndex();
    }

    public String getMessageWithEntryIndex() {
        String columnPart = columnNumber == null ? "" : " " + createMessagePlaceholder("MapperContextEntryError.column") + ":" + columnNumber;
        return String.format("%s:%s%s - %s", createMessagePlaceholder("MapperContextEntryError.entry"),
            entryIndex, columnPart, errorReason);
    }

    public String getMessageWithoutEntryIndex() {
        String columnPart = columnNumber == null ? "" : createMessagePlaceholder("MapperContextEntryError.column") + ":" + columnNumber + ", ";
        return String.format("%s%s", columnPart, errorReason);
    }
}
