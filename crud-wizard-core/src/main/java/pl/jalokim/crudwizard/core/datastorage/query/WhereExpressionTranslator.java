package pl.jalokim.crudwizard.core.datastorage.query;

public interface WhereExpressionTranslator<F> {

    F translateWhereExpression(AbstractExpression expression);

}
