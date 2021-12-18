package pl.jalokim.crudwizard.core.datastorage.query.inmemory;

import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.EQUALS;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.GREATER_THAN;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.IN;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.IS_NOT_NULL;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.IS_NULL;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.LIKE;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.LIKE_IGNORE_CASE;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionType.LOWER_THAN;
import static pl.jalokim.crudwizard.core.datastorage.query.LogicalOperator.AND;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isArrayType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Component
public class InMemoryWhereExpressionTranslator {

    private final static Map<ExpressionType, TriPredicate> PREDICATE_BY_EXPRESSION_TYPE = Map.of(
        LIKE_IGNORE_CASE, (realObject, leftArg, rightArg) ->
            StringUtils.containsIgnoreCase(
                leftArg.extractValue(realObject),
                rightArg.extractValue(realObject)),
        LIKE, (realObject, leftArg, rightArg) ->
            StringUtils.contains(
                leftArg.extractValue(realObject),
                rightArg.extractValue(realObject)),
        EQUALS, (realObject, leftArg, rightArg) ->
            Objects.equals(
                leftArg.extractValue(realObject),
                rightArg.extractValue(realObject)),
        LOWER_THAN, (realObject, leftArg, rightArg) ->
            isLowerThanSecond(expressionArgumentToNumber(realObject, rightArg),
                expressionArgumentToNumber(realObject, leftArg)
            ),
        GREATER_THAN, (realObject, leftArg, rightArg) ->
            isGreaterThanSecond(expressionArgumentToNumber(realObject, rightArg),
                expressionArgumentToNumber(realObject, leftArg)
            ),
        IN, InMemoryWhereExpressionTranslator::objectFromLeftExpressionExistsInRightCollection,
        IS_NULL, (realObject, leftArg, rightArg) -> leftArg.extractValue(realObject) == null,
        IS_NOT_NULL, (realObject, leftArg, rightArg) -> leftArg.extractValue(realObject) != null
    );

    public Predicate<Object> translateWhereExpression(AbstractExpression expression) {
        if (expression == null) {
            return (obj) -> true;
        }

        if (expression instanceof NegatedExpression) {
            NegatedExpression negatedExpression = (NegatedExpression) expression;
            return translateWhereExpression(negatedExpression.getRealExpression()).negate();
        } else if (expression instanceof RealExpression) {
            RealExpression realExpression = (RealExpression) expression;
            ExpressionArgument leftArg = realExpression.getLeftArg();
            ExpressionArgument rightArg = realExpression.getRightArg();
            TriPredicate triPredicate = PREDICATE_BY_EXPRESSION_TYPE.get(realExpression.getOperationType());
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

    private static BigDecimal expressionArgumentToNumber(Object realObject, ExpressionArgument argument) {
        Object someNumber = argument.extractValue(realObject);
        return Optional.ofNullable(someNumber)
            .map(Objects::toString)
            .map(BigDecimal::new)
            .orElse(null);
    }

    private static boolean objectFromLeftExpressionExistsInRightCollection(Object realObject, ExpressionArgument leftExpression,
        ExpressionArgument rightExpression) {
        Object valueFromLeft = leftExpression.extractValue(realObject);
        Object valueFromRight = rightExpression.extractValue(realObject);
        if (valueFromLeft != null && valueFromRight != null) {
            Collection<Object> rightCollection = convertToCollection(valueFromRight);
            return rightCollection.contains(valueFromLeft);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> convertToCollection(Object someValueFromExpression) {
        Class<?> typeForExpression = someValueFromExpression.getClass();
        if (MetadataReflectionUtils.isHavingElementsType(typeForExpression)) {
            if (isCollectionType(typeForExpression)) {
                return (Collection<Object>) someValueFromExpression;
            } else if (isArrayType(typeForExpression)) {
                Object[] array = (Object[]) someValueFromExpression;
                return Arrays.asList(array);
            } else if (Stream.class.isAssignableFrom(typeForExpression)) {
                return elements((Stream<Object>) someValueFromExpression).asList();
            }
        }
        throw new IllegalArgumentException("expected type to be collection type");
    }

    private static boolean isLowerThanSecond(BigDecimal first, BigDecimal second) {
        if (first != null && second != null) {
            return first.compareTo(second) > 0;
        }
        return false;
    }

    private static boolean isGreaterThanSecond(BigDecimal first, BigDecimal second) {
        if (first != null && second != null) {
            return first.compareTo(second) < 0;
        }
        return false;
    }

    private interface TriPredicate {

        boolean test(Object realObject, ExpressionArgument rightArg, ExpressionArgument leftArg);
    }
}
