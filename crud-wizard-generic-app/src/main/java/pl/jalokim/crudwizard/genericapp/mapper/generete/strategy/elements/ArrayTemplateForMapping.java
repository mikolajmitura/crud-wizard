package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import java.util.List;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class ArrayTemplateForMapping implements IterableTemplateForMapping {

    @Override
    public String generateNewIterable(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        return "new " + contactGenerics(genericTypesOfIterable) + "[initSize]";
    }

    @Override
    public String generateIterableType(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        return "        int index = 0;" + System.lineSeparator() +
            "        " + contactGenerics(genericTypesOfIterable) + "[]";
    }

    @Override
    public String getPopulateIterableTemplate() {
        return "elements[index++] = ${expression0}";
    }

    @Override
    public String elementForIterateType(List<ClassMetaModel> genericTypesOfIterable) {
        return contactGenerics(genericTypesOfIterable);
    }

    @Override
    public boolean canMapFromSource(ClassMetaModel sourceClassMetaModel) {
        return sourceClassMetaModel.isListType() || sourceClassMetaModel.isSetType() ||
            sourceClassMetaModel.isArrayType() || !sourceClassMetaModel.isMapType();
    }
}
