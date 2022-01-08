package pl.jalokim.crudwizard.core.datastorage.query;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class QueryExpressionArgument extends ExpressionArgument {

    DataStorageQuery dataStorageQuery;

    @Override
    public <T> T extractValue(Object argumentObject) {
        throw new IllegalStateException("not implemented yet");
    }
}
