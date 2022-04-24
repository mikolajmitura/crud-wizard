package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public class NullAssignExpression extends RawJavaCodeAssignExpression {

    public NullAssignExpression(ClassMetaModel returnClassMetaModel) {
        super(returnClassMetaModel, "null");
    }
}
