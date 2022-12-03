package pl.jalokim.crudwizard.core.sample;

import lombok.EqualsAndHashCode;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField", "PMD.UnusedFormalParameter"})
@EqualsAndHashCode
public class SomeAllArgConstructor extends SomeSuperAllArgConstructor {

    private final String taste;

    public SomeAllArgConstructor(Long type1, String name,
        String taste, Long notField) {

        super(type1, name);
        this.taste = taste;
    }
}
