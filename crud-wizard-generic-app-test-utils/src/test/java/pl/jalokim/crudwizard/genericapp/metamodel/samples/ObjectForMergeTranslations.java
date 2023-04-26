package pl.jalokim.crudwizard.genericapp.metamodel.samples;

import lombok.Value;

@Value
public class ObjectForMergeTranslations {

    Long id;
    String name;
    NestedObject someObject;
    SomeEnumTranslations someType;
}
