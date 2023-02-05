package pl.jalokim.crudwizard.genericapp.metamodel.samples;

import lombok.Value;

@Value
public class SomeRealClass {

    Long id;
    String name;
    NestedObject someObject;
}
