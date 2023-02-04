package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample;

import lombok.Data;

@Data
public class ConcreteDto1 extends SuperDto1 {

    private Long id;

    public String getStr1() {
        return str1;
    }
}
