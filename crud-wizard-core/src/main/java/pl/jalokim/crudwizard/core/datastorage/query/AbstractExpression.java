package pl.jalokim.crudwizard.core.datastorage.query;

public abstract class AbstractExpression {

    abstract LinkedExpression getLinkedExpression();

    public LinkedExpression and(AbstractExpression expression) {
        return getLinkedExpression().linkExpression(LogicalOperator.AND, expression);
    }

    public LinkedExpression or(AbstractExpression expression) {
        return getLinkedExpression().linkExpression(LogicalOperator.OR, expression);
    }

    public final NegatedExpression negate() {
        return new NegatedExpression(this);
    }
}
