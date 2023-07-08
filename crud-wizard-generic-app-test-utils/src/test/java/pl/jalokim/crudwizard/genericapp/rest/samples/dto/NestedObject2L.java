package pl.jalokim.crudwizard.genericapp.rest.samples.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NestedObject2L extends SuperNestedObject {

    private String id;
    private NestedObject3L level3;
}
