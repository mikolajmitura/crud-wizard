package pl.jalokim.crudwizard.core.sample;

import lombok.Value;

@Value
public class ForTestMappingMultiSourceDto {

    SamplePersonDtoWitOtherObject firstPerson;
    SamplePersonDtoWitOtherObject secondPerson;
    DocumentHolderDto docHolder;
    DocumentHolderDto sameMappingLikeDocHolder;
    DocumentHolderDto otherMappingForDocHolder;

    @Value
    public static class DocumentHolderDto {
        SomeDocumentDto document;
    }
}
