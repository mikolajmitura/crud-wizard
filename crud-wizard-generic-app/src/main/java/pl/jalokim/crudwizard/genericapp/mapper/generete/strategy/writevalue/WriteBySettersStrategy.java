package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.firstLetterToUpperCase;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.utils.template.TemplateAsText;

public class WriteBySettersStrategy implements WritePropertyStrategy {

    @Override
    public String generateInitLine(ClassMetaModel targetClassMetaModel) {
        return TemplateAsText.fromText("${className} newObject = new ${className}();")
            .overrideVariable("className", targetClassMetaModel.getJavaGenericTypeInfo())
            .getCurrentTemplateText();
    }

    @Override
    public String generateWritePropertyCode(String fieldName, String fieldValueFetcher) {
        return String.format("newObject.set%s(%s);", firstLetterToUpperCase(fieldName), fieldValueFetcher);
    }

    @Override
    public String generateLastLine(String javaGenericTypeInfo) {
        return wrapValueWithReturnStatement(javaGenericTypeInfo, "newObject");
    }
}
