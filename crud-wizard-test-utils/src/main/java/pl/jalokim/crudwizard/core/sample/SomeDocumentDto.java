package pl.jalokim.crudwizard.core.sample;

import lombok.Value;

@Value
public class SomeDocumentDto {

    InnerDocumentDto documentData;
    Long id;
}
