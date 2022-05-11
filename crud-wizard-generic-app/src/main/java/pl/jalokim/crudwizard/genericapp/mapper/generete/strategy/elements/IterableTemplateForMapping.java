package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

public interface IterableTemplateForMapping {

    String generateNewIterable(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata);

    String generateIterableType(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata);

    String getPopulateIterableTemplate();

    default List<String> getVariablesExpressionsForAddToIterable() {
        return List.of("element");
    }

    String elementForIterateType(List<ClassMetaModel> genericTypesOfIterable);

    default String getExpressionForIterateFrom(String variableName) {
        return variableName;
    }

    default String contactGenerics(List<ClassMetaModel> genericTypesOfIterable) {
        return elements(genericTypesOfIterable)
            .map(ClassMetaModel::getJavaGenericTypeInfo)
            .asConcatText(", ");
    }

    boolean canMapFromSource(ClassMetaModel sourceClassMetaModel);
}
