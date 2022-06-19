package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findOneConstructorMaxArgNumbers;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.util.List;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.ConstructorMetadata;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByAllArgsFieldsResolver implements FieldMetaResolver {

    public static ByAllArgsFieldsResolver INSTANCE = new ByAllArgsFieldsResolver();

    @Override
    public List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        Constructor<?> maxArgConstructor = findOneConstructorMaxArgNumbers(typeMetadata.getRawType());
        if (maxArgConstructor != null) {
            ConstructorMetadata metaForConstructor = typeMetadata.getMetaForConstructor(maxArgConstructor);
            return elements(metaForConstructor.getParameters())
                .map(parameter -> {
                    String fieldName = parameter.getName();
                    return (FieldMetaModel) FieldMetaModel.builder()
                        .fieldName(fieldName)
                        .fieldType(createNotGenericClassMetaModel(parameter.getTypeOfParameter(),
                            fieldMetaResolverConfiguration, fieldName, typeMetadata))
                        .build();
                })
                .asList();
        }
        throw new IllegalArgumentException("given class " + typeMetadata.getRawType()
            + " should have one constructor with max number of arguments");
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.getFields();
    }
}
