package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample;

import lombok.Data;

@Data
public class SuperClass<I, V> {

    private I id;
    private V value;

    public String getSomeString() {
        return null;
    }
}
