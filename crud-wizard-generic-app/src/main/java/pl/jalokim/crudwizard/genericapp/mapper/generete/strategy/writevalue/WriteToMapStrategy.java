package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public class WriteToMapStrategy implements WritePropertyStrategy {

    @Override
    public String generateInitLine(ClassMetaModel targetClassMetaModel) {
        return "Map<String, Object> map = new java.util.HashMap<>();";
    }

    @Override
    public String generateWritePropertyCode(String fieldName, String fieldValueFetcher) {
        return String.format("map.put(\"%s\", %s);", fieldName, fieldValueFetcher);
    }

    @Override
    public String generateLastLine(String javaGenericTypeInfo) {
        return wrapValueWithReturnStatement(javaGenericTypeInfo, "map");
    }
}
