package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import lombok.Getter;

@Getter
public class EntryMappingParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final MapperContextEntryError mapperContextEntryError;
    private final ErrorSource errorType;

    public EntryMappingParseException(MapperContextEntryError mapperContextEntryError) {
        this(ErrorSource.SOURCE_EXPRESSION, mapperContextEntryError);
    }

    public EntryMappingParseException(ErrorSource errorType, MapperContextEntryError mapperContextEntryError) {
        super(mapperContextEntryError.toString());
        this.errorType = errorType;
        this.mapperContextEntryError = mapperContextEntryError;
    }

    public enum ErrorSource {
        SOURCE_EXPRESSION,
        TARGET_EXPRESSION
    }
}
