package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import java.util.Comparator;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

public interface WritePropertyStrategy {

    String generateInitLine(ClassMetaModel targetClassMetaModel);

    String generateWritePropertyCode(String fieldName, String fieldValueFetcher);

    String generateLastLine(String javaGenericTypeInfo);

    default String lastWritePropertyLineChanger(String line) {
        return line;
    }

    default Comparator<FieldMetaModel> getFieldSorter() {
        return Comparator.comparing(FieldMetaModel::getFieldName);
    }
}
