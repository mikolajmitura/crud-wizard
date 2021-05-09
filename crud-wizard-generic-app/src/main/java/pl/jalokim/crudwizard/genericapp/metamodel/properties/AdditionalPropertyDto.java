package pl.jalokim.crudwizard.genericapp.metamodel.properties;

import lombok.Value;

@Value
public class AdditionalPropertyDto {

    Long id;
    String name;
    String valueRealClassName;
    Object value;
}
