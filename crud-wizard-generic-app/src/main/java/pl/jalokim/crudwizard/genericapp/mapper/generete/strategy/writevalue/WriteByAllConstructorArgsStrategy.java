package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import java.util.Comparator;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.utils.string.StringUtils;

public class WriteByAllConstructorArgsStrategy implements WritePropertyStrategy {

    @Override
    public String generateInitLine(ClassMetaModel targetClassMetaModel) {
        return String.format("return new %s(", targetClassMetaModel.getJavaGenericTypeInfo());
    }

    @Override
    public String generateWritePropertyCode(String fieldName, String fieldValueFetcher) {
        return fieldValueFetcher + ",";
    }

    @Override
    public String generateLastLine(String javaGenericTypeInfo) {
        return ")";
    }

    @Override
    public String lastWritePropertyLineChanger(String line) {
        return StringUtils.isNotBlank(line) ? line.substring(0, line.length() - 1) : line;
    }

    @Override
    public Comparator<FieldMetaModel> getFieldSorter() {
        return (field1, field2) -> 0;
    }
}
