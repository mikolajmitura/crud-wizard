package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class NullAssignExpression extends RawJavaCodeAssignExpression {

    public static final String NULL_ASSIGN = "null";

    public NullAssignExpression(ClassMetaModel returnClassMetaModel) {
        super(returnClassMetaModel, NULL_ASSIGN);
    }
}
