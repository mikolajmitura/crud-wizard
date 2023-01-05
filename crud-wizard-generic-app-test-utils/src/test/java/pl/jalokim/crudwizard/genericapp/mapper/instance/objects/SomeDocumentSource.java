package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Value;

@Value
public class SomeDocumentSource<S> {

    Long id;
    S serialNumber;
}
