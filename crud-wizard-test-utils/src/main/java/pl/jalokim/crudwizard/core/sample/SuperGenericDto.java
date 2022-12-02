package pl.jalokim.crudwizard.core.sample;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class SuperGenericDto<T, I, S> {

    @Getter
    @Setter
    private List<T> someListOfT;
    @Getter
    @Setter
    private I objectOfIType;
    @Setter
    private Map<String, Map<Long, S>> mapWithSType;

    public void getResults() {
    }

    public Long getResults(long arg) {
        return arg;
    }

    public I getCopyOfObjectOfTType() {
        return objectOfIType;
    }

    public Long getResults2() {
        return null;
    }
}
