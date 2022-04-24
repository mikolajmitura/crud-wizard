package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

public class EntryMappingParseException extends RuntimeException {

    public EntryMappingParseException(MapperContextEntryError mapperContextEntryError) {
        super(mapperContextEntryError.toString());
    }
}
