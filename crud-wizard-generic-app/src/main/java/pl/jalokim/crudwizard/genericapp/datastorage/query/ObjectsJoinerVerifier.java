package pl.jalokim.crudwizard.genericapp.datastorage.query;

public interface ObjectsJoinerVerifier<F, T> {

    boolean areJoined(F fromObjectValue, T toObjectValue);
}
