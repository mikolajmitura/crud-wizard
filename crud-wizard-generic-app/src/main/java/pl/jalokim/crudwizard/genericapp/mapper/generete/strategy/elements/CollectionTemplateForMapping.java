package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public abstract class CollectionTemplateForMapping implements IterableTemplateForMapping {

    @Override
    public String getPopulateIterableTemplate() {
        return "elements.add(${expression0})";
    }

    @Override
    public String elementForIterateType(List<ClassMetaModel> genericTypesOfIterable) {
        return contactGenerics(genericTypesOfIterable);
    }

    @Override
    public boolean canMapFromSource(ClassMetaModel sourceClassMetaModel) {
        return sourceClassMetaModel.isListType() || sourceClassMetaModel.isSetType()
            || sourceClassMetaModel.isArrayType() || !sourceClassMetaModel.isMapType();
    }
}
