package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Builder;
import pl.jalokim.utils.collection.CollectionUtils;

@Builder
public class ClassMetaModelBuildExpression {

    private static final String INDENTATION = "            ";

    Class<?> realClass;
    List<String> genericTypeExpressions;

    @Override
    public String toString() {
        String genericParts = "";
        if (CollectionUtils.isNotEmpty(genericTypeExpressions)) {
            genericParts = INDENTATION + "\t.genericTypes(List.of(" + elements(genericTypeExpressions).asConcatText(", ") + "))";
        }
        return "ClassMetaModel.builder()" + System.lineSeparator() +
            INDENTATION + "\t.realClass(" + realClass.getCanonicalName() + ".class)" + System.lineSeparator() +
            genericParts + INDENTATION + "\t.build()";
    }
}
