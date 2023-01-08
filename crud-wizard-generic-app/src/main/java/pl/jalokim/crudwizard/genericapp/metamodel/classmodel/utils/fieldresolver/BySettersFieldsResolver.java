package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.methodReturnsVoidAndHasArgumentsSize;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ParameterMetadata;
import pl.jalokim.utils.reflection.TypeMetadata;

public class BySettersFieldsResolver implements FieldMetaResolver {

    public static final BySettersFieldsResolver INSTANCE = new BySettersFieldsResolver();

    @Override
    public List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return elements(MetadataReflectionUtils.getAllDeclaredNotStaticMethods(typeMetadata.getRawType()))
            .filter(method -> method.getName().startsWith("set"))
            .filter(MetadataReflectionUtils::isPublicMethod)
            .filter(method -> methodReturnsVoidAndHasArgumentsSize(method, 1))
            .filter(method -> {
                try {
                    typeMetadata.getMetaForMethod(method);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            })
            .map(typeMetadata::getMetaForMethod)
            .map(methodMetadata -> {
                String fieldName = StringCaseUtils.firstLetterToLowerCase(methodMetadata.getName().substring(3));
                ParameterMetadata parameterMetadata = methodMetadata.getParameters().get(0);

                return (FieldMetaModel) FieldMetaModel.builder()
                    .fieldName(fieldName)
                    .accessFieldType(AccessFieldType.WRITE) // TODO #62 fieldMetaResolverConfiguration.getFieldAccessType()
                    .fieldType(createClassMetaModel(parameterMetadata.getTypeOfParameter(), fieldMetaResolverConfiguration))
                    .build();
            })
            .asList();
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.fetchAllFields();
    }
}
