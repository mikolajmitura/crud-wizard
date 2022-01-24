package pl.jalokim.crudwizard.core.sample;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class SomeMiddleGenericDto<O>
    extends SuperGenericDto<O, Set<Long>, String>
    implements SomeInterfaceDto {

    @Getter
    @Setter
    O objectOfMiddle;

    Long someOtherMiddleField;

    @Override
    public String getSomeString() {
        return null;
    }

    @Override
    public Long getSomeLong() {
        return null;
    }

    public void setMyString(String someString) {

    }

    public void setMyStrings(String someString1, String someString2) {

    }
}
