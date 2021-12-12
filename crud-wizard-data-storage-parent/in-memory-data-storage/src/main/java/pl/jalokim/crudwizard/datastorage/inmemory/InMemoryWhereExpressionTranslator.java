package pl.jalokim.crudwizard.datastorage.inmemory;

import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.LIKE_IGNORE_CASE;
import static pl.jalokim.crudwizard.core.datastorage.query.LogicalOperator.AND;
import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.query.AbstractExpression;
import pl.jalokim.crudwizard.core.datastorage.query.EmptyExpression;
import pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument;
import pl.jalokim.crudwizard.core.datastorage.query.ExpressionType;
import pl.jalokim.crudwizard.core.datastorage.query.LinkedExpression;
import pl.jalokim.crudwizard.core.datastorage.query.LogicalOperator;
import pl.jalokim.crudwizard.core.datastorage.query.NegatedExpression;
import pl.jalokim.crudwizard.core.datastorage.query.RealExpression;

@Component
public class InMemoryWhereExpressionTranslator {

    private static Map<ExpressionType, TriPredicate> predicateByExpressionType = Map.of(
        LIKE_IGNORE_CASE, (realObject, leftArg, rightArg) -> StringUtils.containsIgnoreCase(
            expressionArgumentToText(realObject, leftArg), expressionArgumentToText(realObject, rightArg))
    );

    public Predicate<Object> translateWhereExpression(AbstractExpression expression) {
        if (expression instanceof NegatedExpression) {
            NegatedExpression negatedExpression = (NegatedExpression) expression;
            return translateWhereExpression(negatedExpression.getRealExpression()).negate();
        } else if (expression instanceof RealExpression) {
            RealExpression realExpression = (RealExpression) expression;
            ExpressionArgument leftArg = realExpression.getLeftArg();
            ExpressionArgument rightArg = realExpression.getRightArg();
            TriPredicate triPredicate = predicateByExpressionType.get(realExpression.getOperationType());
            return object -> triPredicate.test(object, leftArg, rightArg);
        } else if (expression instanceof LinkedExpression) {
            LinkedExpression linkedExpression = (LinkedExpression) expression;
            Predicate<Object> currentPredicate = translateWhereExpression(linkedExpression.getInitExpression());
            List<AbstractExpression> expressions = linkedExpression.getExpressions();
            for (int i = 1; i < expressions.size(); i++) {
                AbstractExpression abstractExpression = expressions.get(i);
                LogicalOperator logicalOperator = linkedExpression.getLogicalOperatorsForExpressions().get(i - 1);
                if (AND.equals(logicalOperator)) {
                    currentPredicate = currentPredicate.and(translateWhereExpression(abstractExpression));
                } else {
                    currentPredicate = currentPredicate.or(translateWhereExpression(abstractExpression));
                }
            }
            return currentPredicate;
        } else if (expression instanceof EmptyExpression) {
            return object -> true;
        }
        throw new IllegalArgumentException("Unsupported expression class: " + expression.getClass().getCanonicalName());
    }

    private static String expressionArgumentToText(Object realObject, ExpressionArgument argument) {
        if (argument.getRealValue() != null && argument.getRealValue() instanceof String) {
            return (String) argument.getRealValue();
        } else if (argument.getFullPath() != null) {
            return (String) getValueFromPath(realObject, argument.getFullPath());
        } else if (argument.getDataStorageQuery() != null) {
            throw new IllegalStateException("not supported DataStorageQuery in where expression for text");
        }
        throw new IllegalStateException("Supported only text value and field should be text type");
    }

    private interface TriPredicate {

        boolean test(Object realObject, ExpressionArgument rightArg, ExpressionArgument leftArg);
    }
}
