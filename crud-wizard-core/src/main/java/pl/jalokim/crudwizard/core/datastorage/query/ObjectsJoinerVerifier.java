package pl.jalokim.crudwizard.core.datastorage.query;

public interface ObjectsJoinerVerifier<F, T> {

    boolean areJoined(F fromObjectValue, T toObjectValue);
}
