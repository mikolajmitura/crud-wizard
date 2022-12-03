package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.methodReturnsNonVoidAndHasArgumentsSize;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllDeclaredNotStaticMethods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByBuilderFieldsResolver implements FieldMetaResolver {

    public static final ByBuilderFieldsResolver INSTANCE = new ByBuilderFieldsResolver();

    @Override
    public List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        Class<?> rawClass = typeMetadata.getRawClass();
        try {
            Method builderMethod = rawClass.getDeclaredMethod("builder");
            Class<?> builderClass = builderMethod.getReturnType();

            TypeMetadata builderTypeMetadata;
            if (Modifier.isAbstract(builderClass.getModifiers())) {
                Class<?> notAbstractBuilderClass = ClassUtils.loadRealClass(builderClass.getCanonicalName() + "Impl");
                builderTypeMetadata = MetadataReflectionUtils.getTypeMetadataFromClass(notAbstractBuilderClass);
            } else {
                builderTypeMetadata = MetadataReflectionUtils.getTypeMetadataFromClass(builderClass);
            }

            if (builderMethod.getParameterCount() != 0) {
                throw new NoSuchMethodException("builder method with not empty arguments list");
            }

            return elements(getAllDeclaredNotStaticMethods(builderClass))
                .filter(method -> methodReturnsNonVoidAndHasArgumentsSize(method, 1))
                .filter(MetadataReflectionUtils::isPublicMethod)
                .filter(method -> method.getReturnType().equals(builderClass))
                .map(builderTypeMetadata::getMetaForMethod)
                .map(methodMetadata -> {
                    String fieldName = methodMetadata.getName();
                    return (FieldMetaModel) FieldMetaModel.builder()
                        .fieldName(fieldName)
                        .fieldType(createNotGenericClassMetaModel(methodMetadata.getParameters()
                                .get(0).getTypeOfParameter(),
                            fieldMetaResolverConfiguration, fieldName, typeMetadata))
                        .build();
                })
                .asList();

        } catch (NoSuchMethodException e) {
            throw new TechnicalException(createMessagePlaceholder(
                "cannot.find.builder.method.in.class", typeMetadata.getCanonicalName()), e);
        }
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        Class<?> rawClass = classMetaModel.getRealClassOrBasedOn();

        if (hasSuperBuilderType(rawClass)) {
            List<FieldMetaModel> fieldMetaModels = new ArrayList<>(classMetaModel.getFields());
            ClassMetaModel currentModel = classMetaModel;
            while (isNotEmpty(currentModel.getExtendsFromModels())) {
                ClassMetaModel superMetaModel = currentModel.getExtendsFromModels().get(0);
                if (hasSuperBuilderType(superMetaModel.getRealClassOrBasedOn())) {
                    currentModel = superMetaModel;
                    fieldMetaModels.addAll(currentModel.getFields());
                } else {
                    break;
                }
            }
            return fieldMetaModels;
        } else {
            return classMetaModel.getFields();
        }
    }

    private boolean hasSuperBuilderType(Class<?> rawClass) {
        try {
            Method builderMethod = rawClass.getDeclaredMethod("builder");
            Class<?> builderClass = builderMethod.getReturnType();

            return Modifier.isAbstract(builderClass.getModifiers());
        } catch (NoSuchMethodException e) {
            throw new TechnicalException(createMessagePlaceholder(
                "cannot.find.builder.method.in.class", rawClass.getCanonicalName()), e);
        }
    }
}
