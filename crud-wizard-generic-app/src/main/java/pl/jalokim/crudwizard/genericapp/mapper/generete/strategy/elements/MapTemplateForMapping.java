package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class MapTemplateForMapping implements IterableTemplateForMapping {

    @Override
    public String generateNewIterable(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(HashMap.class);
        return "new HashMap<>(initSize)";
    }

    @Override
    public String generateIterableType(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(Map.class);
        mapperCodeMetadata.addImport(Map.Entry.class);
        return "        Map<" + contactGenerics(genericTypesOfIterable) + ">";
    }

    @Override
    public String getPopulateIterableTemplate() {
        return "elements.put(${expression0}, ${expression1})";
    }

    @Override
    public List<String> getVariablesExpressionsForAddToIterable() {
        return List.of("element.getKey()", "element.getValue()");
    }

    @Override
    public String elementForIterateType(List<ClassMetaModel> genericTypesOfIterable) {
        return "Entry<" + contactGenerics(genericTypesOfIterable) + ">";
    }

    @Override
    public String getExpressionForIterateFrom(String variableName) {
        return variableName + ".entrySet()";
    }

    @Override
    public boolean canMapFromSource(ClassMetaModel sourceClassMetaModel) {
        return sourceClassMetaModel.isMapType();
    }
}
