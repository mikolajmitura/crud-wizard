package pl.jalokim.crudwizard.genericapp.datastorage.query;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath;

@EqualsAndHashCode(callSuper = false)
@Value
public class FullPathExpressionArgument extends ExpressionArgument {

    String fullPath;

    @Override
    public <T> T extractValue(Object argumentObject) {
        return ValueExtractorFromPath.getValueFromPath(argumentObject, fullPath);
    }
}
