package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@UtilityClass
public class ClassMetamodelDescriber {

    public static String getFullDescription(ClassMetaModel classMetaModel) {
        if (classMetaModel.isGenericModel()) {
            return "genericModel name: " + classMetaModel.getName();
        }
        return "real class: " + classMetaModel.getJavaGenericTypeInfo();
    }

    public static String getFullDescription(List<MapperArgumentMethodModel> methodArguments) {
        return elements(methodArguments)
            .map(MapperArgumentMethodModel::getArgumentType)
            .map(ClassMetamodelDescriber::getFullDescription)
            .asConcatText("and ");
    }
}
