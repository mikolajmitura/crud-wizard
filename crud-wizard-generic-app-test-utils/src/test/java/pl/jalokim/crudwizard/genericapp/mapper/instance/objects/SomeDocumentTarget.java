package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Value;

@Value
public class SomeDocumentTarget<N, G> {

    String uuid;
    N number;
    G generatedBy;
}
