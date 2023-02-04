package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findReadFieldMetaResolver;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findWriteFieldMetaResolver;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ReadFieldResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.WriteFieldResolver;
import pl.jalokim.utils.reflection.TypeMetadata;

public class AllAccessFieldMetaModelResolver {

    public static void resolveFieldsForWholeHierarchy(ClassMetaModel classMetaModel) {
        FieldMetaResolverConfiguration fieldMetaResolverConfiguration = classMetaModel.getFieldMetaResolverConfiguration();

        if (classMetaModel.isOnlyRawClassModel()) {
            TypeMetadata typeMetadata = classMetaModel.getTypeMetadata();

            if (!typeMetadata.isSimpleType() &&
                !typeMetadata.getRawType().equals(Object.class) &&
                !typeMetadata.rawClassIsComingFromJavaApi()) {
                ReadFieldResolver readFieldMetaResolver = findReadFieldMetaResolver(typeMetadata.getRawType(), fieldMetaResolverConfiguration);
                classMetaModel.setReadFieldResolver(readFieldMetaResolver);
                readFieldMetaResolver.resolveReadFields(classMetaModel, fieldMetaResolverConfiguration);

                WriteFieldResolver writeFieldMetaResolver = findWriteFieldMetaResolver(typeMetadata.getRawType(), fieldMetaResolverConfiguration);
                classMetaModel.setWriteFieldResolver(writeFieldMetaResolver);
                writeFieldMetaResolver.resolveWriteFields(classMetaModel, fieldMetaResolverConfiguration);
            }
        }
    }
}
