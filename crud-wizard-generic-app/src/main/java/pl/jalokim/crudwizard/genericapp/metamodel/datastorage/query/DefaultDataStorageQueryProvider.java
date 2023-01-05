package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query;

import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.asUnderscoreLowercase;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionArgument.buildForPath;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.EQUALS;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.GREATER_THAN;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.IN;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.IS_NOT_NULL;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.IS_NULL;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.LIKE;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.LIKE_IGNORE_CASE;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType.LOWER_THAN;
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.likeIgnoreCase;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import pl.jalokim.crudwizard.genericapp.datastorage.query.AbstractExpression;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.datastorage.query.EmptyExpression;
import pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionArgument;
import pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionType;
import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.collection.CollectionUtils;

public class DefaultDataStorageQueryProvider implements DataStorageQueryProvider {

    public static final String EXPRESSION_TYPE = "#expression_type";
    public static final String EXPRESSION_RIGHT_PATH = "#expression_right_path";
    public static final String EXPRESSION_LEFT_PATH = "#expression_left_path";
    public static final String IGNORE_IN_QUERY_PARAM = "#ignore_in_query_as_param";

    private static final Map<ExpressionType, BiFunction<ExpressionArgument, ExpressionArgument, AbstractExpression>> EXPRESSION_BY_TYPE = Map.of(
        LIKE_IGNORE_CASE, RealExpression::likeIgnoreCase,
        LIKE, RealExpression::like,
        EQUALS, RealExpression::isEqualsTo,
        LOWER_THAN, RealExpression::lowerThan,
        GREATER_THAN, RealExpression::greaterThan,
        IN, RealExpression::in,
        IS_NULL, (leftArg, rightArg) -> RealExpression.isNull(leftArg),
        IS_NOT_NULL, (leftArg, rightArg) -> RealExpression.isNotNull(leftArg)
    );

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {
        ClassMetaModel classMetaModelFromDataStore = CollectionUtils.getFirst(dataStorageQueryArguments.getQueriedClassMetaModels());
        Map<String, Object> requestParams = dataStorageQueryArguments.getRequestParams();
        ClassMetaModel requestParamsClassMetaModel = dataStorageQueryArguments.getRequestParamsClassMetaModel();

        AbstractExpression whereExpression = new EmptyExpression();

        if (requestParams != null && requestParamsClassMetaModel != null) {
            for (FieldMetaModel field : requestParamsClassMetaModel.fetchAllFields()) {
                Boolean ignoreField = Optional.ofNullable((String) field.getPropertyRealValue(IGNORE_IN_QUERY_PARAM))
                    .map(Boolean::valueOf)
                    .orElse(false);

                if (ignoreField) {
                    continue;
                }

                String expressionType = field.getPropertyRealValue(EXPRESSION_TYPE);
                String rightValueAsPath = field.getPropertyRealValue(EXPRESSION_RIGHT_PATH);
                String leftValueAsPath = field.getPropertyRealValue(EXPRESSION_LEFT_PATH);
                ExpressionArgument leftArg = buildForPath(Optional.ofNullable(leftValueAsPath)
                    .orElse(field.getFieldName()));
                ExpressionArgument rightArg;
                if (rightValueAsPath == null) {
                    rightArg = Optional.ofNullable(requestParams.get(asUnderscoreLowercase(field.getFieldName())))
                        .map(ExpressionArgument::buildForValue).orElse(null);
                } else {
                    rightArg = buildForPath(rightValueAsPath);
                }
                if (rightArg != null) {
                    if (expressionType == null) {
                        whereExpression = whereExpression.and(likeIgnoreCase(leftArg, rightArg));
                    } else {
                        whereExpression = whereExpression.and(EXPRESSION_BY_TYPE.get(ExpressionType.valueOf(expressionType))
                            .apply(leftArg, rightArg));
                    }
                }
            }
        }

        return DataStorageQuery.builder()
            .selectFrom(classMetaModelFromDataStore)
            .where(whereExpression)
            .sortBy(dataStorageQueryArguments.getSortBy())
            .pageable(dataStorageQueryArguments.getPageable())
            .build();
    }
}
