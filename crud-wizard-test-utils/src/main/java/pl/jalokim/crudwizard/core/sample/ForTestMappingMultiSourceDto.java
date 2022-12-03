package pl.jalokim.crudwizard.core.sample;

import lombok.Data;
import lombok.Value;

@Value
public class ForTestMappingMultiSourceDto {

    SamplePersonDtoWitOtherObject firstPerson;
    SamplePersonDtoWitOtherObject secondPerson;
    DocumentHolderDto docHolder;
    DocumentHolderDto sameMappingLikeDocHolder;
    DocumentHolderDto otherMappingForDocHolder;
    TestCurrentNodeObject testCurrentNodeObject;
    String simpleCurrentNodeTest;

    @Value
    public static class DocumentHolderDto {
        SomeDocumentDto document;
    }

    @Data
    public static class TestCurrentNodeObject {
        String uuid;
    }

    @Data
    public static class TestCurrentNodeObjectInModel {
        String uuid;
    }
}
