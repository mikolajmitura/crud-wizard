package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class IterableTemplateForMappingResolver {

    public static IterableTemplateForMapping findIterableTemplateForMappingFor(ClassMetaModel classMetaModel) {
        if (classMetaModel.isListType()) {
            return new ListTemplateForMapping();
        } else if (classMetaModel.isSetType()) {
            return new SetTemplateForMapping();
        } else if (classMetaModel.isMapType()) {
            return new MapTemplateForMapping();
        } else if (classMetaModel.isArrayType()) {
            return new ArrayTemplateForMapping();
        }
        return null;
    }
}
