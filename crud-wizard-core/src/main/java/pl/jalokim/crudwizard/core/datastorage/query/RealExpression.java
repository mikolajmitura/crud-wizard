package pl.jalokim.crudwizard.core.datastorage.query;

import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument.buildForDataStorageQuery;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument.buildForPath;
import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument.buildForValue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class RealExpression extends AbstractExpression {

    ExpressionArgument leftArg;
    ExpressionArgument rightArg;
    ExpressionType operationType;

    @Override
    LinkedExpression getLinkedExpression() {
        return new LinkedExpression(this);
    }

    public static RealExpression likeIgnoreCase(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.LIKE_IGNORE_CASE);
    }

    public static RealExpression likeIgnoreCase(String leftPath, Object expectedValue) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValue), ExpressionType.LIKE_IGNORE_CASE);
    }

    public static RealExpression likeIgnoreCase(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.LIKE_IGNORE_CASE);
    }

    public static RealExpression like(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.LIKE);
    }

    public static RealExpression like(String leftPath, Object expectedValue) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValue), ExpressionType.LIKE);
    }

    public static RealExpression like(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.LIKE);
    }

    public static RealExpression isEqualsTo(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.EQUALS);
    }

    public static RealExpression isEqualsTo(String leftPath, Object expectedValue) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValue), ExpressionType.EQUALS);
    }

    public static RealExpression isEqualsTo(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.EQUALS);
    }

    public static RealExpression lowerThan(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.LOWER_THAN);
    }

    public static RealExpression lowerThan(String leftPath, Object expectedValue) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValue), ExpressionType.LOWER_THAN);
    }

    public static RealExpression lowerThan(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.LOWER_THAN);
    }

    public static RealExpression greaterThan(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.GREATER_THAN);
    }

    public static RealExpression greaterThan(String leftPath, Object expectedValue) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValue), ExpressionType.GREATER_THAN);
    }

    public static RealExpression greaterThan(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.GREATER_THAN);
    }

    public static RealExpression in(ExpressionArgument leftArg, ExpressionArgument rightArg) {
        return new RealExpression(leftArg, rightArg, ExpressionType.IN);
    }

    public static RealExpression in(String leftPath, List<Object> expectedValues) {
        return new RealExpression(buildForPath(leftPath), buildForValue(expectedValues), ExpressionType.IN);
    }

    public static RealExpression in(String leftPath, DataStorageQuery dataStorageSubQuery) {
        return new RealExpression(buildForPath(leftPath), buildForDataStorageQuery(dataStorageSubQuery), ExpressionType.IN);
    }

    public static RealExpression isNull(ExpressionArgument leftArg) {
        return new RealExpression(leftArg, null, ExpressionType.IS_NULL);
    }

    public static RealExpression isNull(String leftPath) {
        return new RealExpression(buildForPath(leftPath), null, ExpressionType.IS_NULL);
    }

    public static RealExpression isNotNull(ExpressionArgument leftArg) {
        return new RealExpression(leftArg, null, ExpressionType.IS_NOT_NULL);
    }

    public static RealExpression isNotNull(String leftPath) {
        return new RealExpression(buildForPath(leftPath), null, ExpressionType.IS_NOT_NULL);
    }
}
