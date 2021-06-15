package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AdditionalPropertyDto {

    Long id;
    String name;
    String valueRealClassName;
    Object value;
}
