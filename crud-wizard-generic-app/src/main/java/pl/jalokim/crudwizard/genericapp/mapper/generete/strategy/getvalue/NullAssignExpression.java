package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

public class NullAssignExpression implements ValueToAssignExpression {

    public static final NullAssignExpression NULL_EXPRESSION = new NullAssignExpression();

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata() {
        return ValueToAssignCodeMetadata.builder()
            .valueGettingCode("null")
            .build();
    }
}
