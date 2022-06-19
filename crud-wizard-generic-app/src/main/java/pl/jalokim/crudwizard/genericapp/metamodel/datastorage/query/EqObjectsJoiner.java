package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query;

import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier;

public class EqObjectsJoiner implements ObjectsJoinerVerifier<Object, Object> {

    @Override
    public boolean areJoined(Object fromObjectValue, Object toObjectValue) {
        if (!fromObjectValue.getClass().equals(toObjectValue.getClass())) {
            return fromObjectValue.toString().equals(toObjectValue.toString());
        }
        return fromObjectValue.equals(toObjectValue);
    }
}
