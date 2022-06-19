package pl.jalokim.crudwizard.genericapp.datastorage.query;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class EmptyExpression extends AbstractExpression {

    @Override
    LinkedExpression getLinkedExpression() {
        return null;
    }

    @Override
    public LinkedExpression and(AbstractExpression expression) {
        return new LinkedExpression(expression);
    }

    @Override
    public LinkedExpression or(AbstractExpression expression) {
        return new LinkedExpression(expression);
    }
}
