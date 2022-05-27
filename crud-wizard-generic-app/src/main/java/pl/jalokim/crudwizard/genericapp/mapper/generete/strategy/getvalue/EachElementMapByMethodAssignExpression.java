package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

@Value
public class EachElementMapByMethodAssignExpression implements ValueToAssignExpression {

    String innerMethodName;

    /**
     * Should be used when expression is source collection, array.
     * But can be used for object as well.
     */
    ValueToAssignExpression wrappedExpression;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {
        return wrappedExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
    }
}
