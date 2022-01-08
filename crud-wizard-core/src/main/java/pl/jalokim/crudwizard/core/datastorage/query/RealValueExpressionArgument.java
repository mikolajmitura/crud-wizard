package pl.jalokim.crudwizard.core.datastorage.query;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class RealValueExpressionArgument extends ExpressionArgument {

    Object realValue;

    @Override
    public <T> T extractValue(Object argumentObject) {
        return (T) realValue;
    }
}
