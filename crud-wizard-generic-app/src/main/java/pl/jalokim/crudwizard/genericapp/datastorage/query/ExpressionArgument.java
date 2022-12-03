package pl.jalokim.crudwizard.genericapp.datastorage.query;

public abstract class ExpressionArgument {

    public static ExpressionArgument buildForPath(String path) {
        return new FullPathExpressionArgument(path);
    }

    public static ExpressionArgument buildForValue(Object realValue) {
        return new RealValueExpressionArgument(realValue);
    }

    public static ExpressionArgument buildForDataStorageQuery(DataStorageQuery dataStorageQuery) {
        return new QueryExpressionArgument(dataStorageQuery);
    }

    public abstract <T> T extractValue(Object argumentObject);

}
