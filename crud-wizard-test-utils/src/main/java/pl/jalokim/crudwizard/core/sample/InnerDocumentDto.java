package pl.jalokim.crudwizard.core.sample;

import lombok.Value;

@Value
public class InnerDocumentDto {

    String serialNumber;
    String signedBy;
    Long docId;
}
