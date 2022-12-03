package pl.jalokim.crudwizard.core.sample;

import lombok.Data;

@Data
public class SomeSuperAllArgConstructor {

    private Long type1;
    private String name;

    public SomeSuperAllArgConstructor(Long type1, String name) {
        this.type1 = type1;
        this.name = name;
    }

    public SomeSuperAllArgConstructor(String name, Long type1) {
        this.type1 = type1;
        this.name = name;
    }
}
