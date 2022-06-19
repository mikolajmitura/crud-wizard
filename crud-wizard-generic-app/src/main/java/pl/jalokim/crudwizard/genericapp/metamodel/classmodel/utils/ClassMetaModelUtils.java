package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;

import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

public class ClassMetaModelUtils {

    public static FieldMetaModel getRequiredFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return createNotGenericClassMetaModel(genericClassMetaModel, fieldMetaResolverConfig)
            .getRequiredFieldByName(fieldName);
    }

    public static FieldMetaModel getFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return createNotGenericClassMetaModel(genericClassMetaModel, fieldMetaResolverConfig)
            .getFieldByName(fieldName);
    }
}
