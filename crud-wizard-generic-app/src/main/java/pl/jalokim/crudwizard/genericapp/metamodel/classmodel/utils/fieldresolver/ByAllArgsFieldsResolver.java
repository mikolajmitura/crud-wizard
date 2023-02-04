package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findOneConstructorMaxArgNumbers;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.util.List;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.ConstructorMetadata;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByAllArgsFieldsResolver implements WriteFieldResolver {

    public static final ByAllArgsFieldsResolver INSTANCE = new ByAllArgsFieldsResolver();

    @Override
    public void resolveWriteFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        TypeMetadata typeMetadata = classMetaModel.getTypeMetadata();
        Constructor<?> maxArgConstructor = findOneConstructorMaxArgNumbers(typeMetadata.getRawType());
        if (maxArgConstructor != null) {
            ConstructorMetadata metaForConstructor = typeMetadata.getMetaForConstructor(maxArgConstructor);
            List<FieldMetaModel> fieldMetaModels = elements(metaForConstructor.getParameters())
                .map(parameter -> {
                    String fieldName = parameter.getName();
                    return (FieldMetaModel) FieldMetaModel.builder()
                        .fieldName(fieldName)
                        .accessFieldType(AccessFieldType.WRITE)
                        .fieldType(createClassMetaModel(parameter.getTypeOfParameter(), fieldMetaResolverConfiguration))
                        .build();
                })
                .asList();
            classMetaModel.mergeFields(fieldMetaModels);
            return;
        }
        throw new IllegalArgumentException("given class " + typeMetadata.getRawType() +
            " should have one constructor with max number of arguments");
    }
}
