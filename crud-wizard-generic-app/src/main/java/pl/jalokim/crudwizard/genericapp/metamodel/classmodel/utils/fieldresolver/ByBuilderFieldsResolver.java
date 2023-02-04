package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.methodReturnsNonVoidAndHasArgumentsSize;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllDeclaredNotStaticMethods;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByBuilderFieldsResolver implements WriteFieldResolver {

    public static final ByBuilderFieldsResolver INSTANCE = new ByBuilderFieldsResolver();

    @Override
    public void resolveWriteFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        Class<?> rawClass = classMetaModel.getRealClass();
        if (hasSuperBuilderType(rawClass)) {
            ClassMetaModel currentModel = classMetaModel;
            while (isNotEmpty(currentModel.getExtendsFromModels())) {
                ClassMetaModel superMetaModel = currentModel.getExtendsFromModels().get(0);
                if (hasSuperBuilderType(superMetaModel.getRealClass())) {
                    currentModel = superMetaModel;
                    classMetaModel.mergeFields(findFields(currentModel.getTypeMetadata(), fieldMetaResolverConfiguration));
                } else {
                    break;
                }
            }
        }
        classMetaModel.mergeFields(findFields(classMetaModel.getTypeMetadata(), fieldMetaResolverConfiguration));
    }

    private List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        Class<?> rawClass = typeMetadata.getRawClass();
        try {
            Method builderMethod = rawClass.getDeclaredMethod("builder");
            Class<?> builderClass = builderMethod.getReturnType();

            TypeMetadata builderTypeMetadata;
            if (Modifier.isAbstract(builderClass.getModifiers())) {
                Class<?> notAbstractBuilderClass = ClassUtils.loadRealClass(builderClass.getCanonicalName() + "Impl");
                builderTypeMetadata = addGenericsFromNotBuilderClass(getTypeMetadataFromClass(notAbstractBuilderClass), typeMetadata);
            } else {
                builderTypeMetadata = addGenericsFromNotBuilderClass(getTypeMetadataFromClass(builderClass), typeMetadata);
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
                        .accessFieldType(AccessFieldType.WRITE)
                        .fieldType(createClassMetaModel(methodMetadata.getParameters()
                                .get(0).getTypeOfParameter(),
                            fieldMetaResolverConfiguration))
                        .build();
                })
                .asList();

        } catch (NoSuchMethodException e) {
            throw new TechnicalException(createMessagePlaceholder(
                "cannot.find.builder.method.in.class", typeMetadata.getCanonicalName()), e);
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

    private TypeMetadata addGenericsFromNotBuilderClass(TypeMetadata builderTypeMetadata, TypeMetadata notBuilderTypeMetadata) {
        if (notBuilderTypeMetadata.hasGenericTypes()) {
            return InvokableReflectionUtils.invokeStaticMethod(TypeMetadata.class, "newTypeMetadata", builderTypeMetadata.getRawClass(),
                notBuilderTypeMetadata.getGenericTypes());
        }
        return builderTypeMetadata;
    }
}
