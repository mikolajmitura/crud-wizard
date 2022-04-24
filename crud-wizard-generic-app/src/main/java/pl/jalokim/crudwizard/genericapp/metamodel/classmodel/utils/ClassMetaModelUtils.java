package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;

public class ClassMetaModelUtils {

    public static FieldMetaModel getRequiredFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return createNotGenericClassMetaModel(genericClassMetaModel, fieldMetaResolverConfig)
            .getRequiredFieldByName(fieldName);
    }

}
