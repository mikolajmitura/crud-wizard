package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel.getGenericsPartToString;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class WriteByBuilderStrategy implements WritePropertyStrategy {

    @Override
    public String generateInitLine(ClassMetaModel targetClassMetaModel) {
        return String.format("return %s.%sbuilder()", targetClassMetaModel.getRealClass().getCanonicalName(),
            getGenericsPartToString(targetClassMetaModel.getGenericTypes()));
    }

    @Override
    public String generateWritePropertyCode(String fieldName, String fieldValueFetcher) {
        return String.format(".%s(%s)", fieldName, fieldValueFetcher);
    }

    @Override
    public String generateLastLine(String javaGenericTypeInfo) {
        return ".build()";
    }

}
