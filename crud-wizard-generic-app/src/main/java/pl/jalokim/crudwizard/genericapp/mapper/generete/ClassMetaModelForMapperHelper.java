package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@UtilityClass
public class ClassMetaModelForMapperHelper {

    public static String getClassModelInfoForGeneratedCode(List<MapperArgumentMethodModel> methodArguments) {
        return elements(methodArguments)
            .map(argument -> getClassModelInfoForGeneratedCode(argument.getArgumentType()))
            .asConcatText("And");
    }
    public static String getClassModelInfoForGeneratedCode(ClassMetaModel classMetaModel) {
        List<String> typeInfoParts = new ArrayList<>();
        if (classMetaModel.getName() != null) {
            typeInfoParts.add("M" + classMetaModel.getName());
        }
        if (classMetaModel.getRealClass() != null) {
            typeInfoParts.add("C" + classMetaModel.getRealClass().getSimpleName());
        }
        return elements(typeInfoParts).asConcatText("_");
    }
}
