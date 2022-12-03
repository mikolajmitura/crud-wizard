package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import lombok.Getter;

public class SomeClassWithPrivateFields {

    @SuppressWarnings("PMD.UnusedPrivateField")
    private Long id;

    @Getter
    private String uuid;
}
