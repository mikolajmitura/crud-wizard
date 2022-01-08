package pl.jalokim.crudwizard.core.validation.javax.inner;

import lombok.Value;

@Value
public class FieldMeta {

    String fieldName;
    Object value;
    String ownerTypeName;
}
