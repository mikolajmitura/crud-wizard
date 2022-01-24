package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@UtilityClass
public class ClassMetaModelDescribeHelper {

    public static String getClassModelInfoForGeneratedCode(ClassMetaModel classMetaModel) {
        List<String> typeInfoParts = new ArrayList<>();
        if (classMetaModel.getName() != null) {
            typeInfoParts.add("Model_" + classMetaModel.getName());
        }
        if (classMetaModel.getRealClass() != null) {
            typeInfoParts.add("RawClass_" + classMetaModel.getRealClass().getSimpleName());
        }
        return elements(typeInfoParts).asConcatText("_");
    }
}
