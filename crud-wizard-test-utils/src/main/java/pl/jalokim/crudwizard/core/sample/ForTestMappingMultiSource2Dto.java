package pl.jalokim.crudwizard.core.sample;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.Value;

@Value
public class ForTestMappingMultiSource2Dto {

    SamplePersonDtoWitOtherObject firstPerson;
    SamplePersonDtoWitOtherObject secondPerson;
    DocumentHolderDto docHolder;
    DocumentHolderDto sameMappingLikeDocHolder;
    DocumentHolderDto otherMappingForDocHolder;
    OtherObject otherObject1;
    OtherObject otherObject2;
    Long someNumber;

    @Value
    public static class DocumentHolderDto {
        SomeDocumentDto document;
    }

    @Value
    public static class SomeEntryDto {
        Long id;
        String name;
        LocalDate someLocaleDate;
        Period somePeriod;
    }

    @Value
    public static class OtherObject {
        String field1;
        Long someLong;
        Integer someInt;
        LocalDateTime someLocalDateTime;
        Long someNumberValue;
    }
}
