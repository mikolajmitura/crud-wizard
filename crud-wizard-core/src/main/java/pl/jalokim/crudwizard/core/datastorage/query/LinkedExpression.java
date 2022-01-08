package pl.jalokim.crudwizard.core.datastorage.query;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class LinkedExpression extends AbstractExpression {

    private final List<AbstractExpression> expressions = new ArrayList<>();
    private final List<LogicalOperator> logicalOperatorsForExpressions = new ArrayList<>();

    LinkedExpression(AbstractExpression expression) {
        expressions.add(expression);
    }

    public LinkedExpression linkExpression(LogicalOperator logicalOperator, AbstractExpression expression) {
        logicalOperatorsForExpressions.add(logicalOperator);
        expressions.add(expression);
        return this;
    }

    public AbstractExpression getInitExpression() {
        return expressions.get(0);
    }

    @Override
    LinkedExpression getLinkedExpression() {
        return this;
    }

}
