package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByDeclaredFieldsResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.string.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class ClassMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;

    String name;

    String className;

    Boolean simpleRawClass;

    Class<?> realClass;

    /**
     * It means that this metamodel was generated upon this class.
     */
    Class<?> basedOnClass;
    EnumClassMetaModel enumClassMetaModel;

    @Builder.Default
    List<ClassMetaModel> genericTypes = new ArrayList<>();
    @Builder.Default
    List<FieldMetaModel> fields = new ArrayList<>();
    @Builder.Default
    List<ValidatorMetaModel> validators = new ArrayList<>();

    @Builder.Default
    List<ClassMetaModel> extendsFromModels = new ArrayList<>();

    /**
     * for cache purposes parent fields metamodel parent validators
     */
    @Setter(AccessLevel.NONE)
    ParentMetamodelCacheContext parentMetamodelCacheContext;

    ClassMetaModelState state;

    /**
     * when true then does it mean that this meta model is like generic enum metamodel
     */
    public boolean isGenericMetamodelEnum() {
        return enumClassMetaModel != null;
    }

    public boolean isEnumTypeOrJavaEnum() {
        return isGenericMetamodelEnum() || isRealClassEnum();
    }

    public FieldMetaModel getFieldByName(String fieldName) {
        if (parentMetamodelCacheContext == null || parentMetamodelCacheContext.getFieldMetaModels() == null
            || parentMetamodelCacheContext.getFieldsByName() == null) {
            fetchAllFields();
        }

        return parentMetamodelCacheContext.getFieldsByName().get(fieldName);
    }

    public FieldMetaModel getRequiredFieldByName(String fieldName) {
        return Optional.ofNullable(getFieldByName(fieldName))
            .orElseThrow(() ->
                new TechnicalException(createMessagePlaceholder("cannot.find.field.name",
                    fieldName, getTypeDescription()))
            );
    }

    public Set<String> getFieldNames() {
        return elements(fetchAllFields())
            .map(FieldMetaModel::getFieldName)
            .asSet();
    }

    public List<FieldMetaModel> getFields() {
        return fields;
    }

    public List<ValidatorMetaModel> getValidators() {
        List<ValidatorMetaModel> validatorMetaModels = Optional.ofNullable(parentMetamodelCacheContext)
            .map(ParentMetamodelCacheContext::getAllValidators)
            .orElse(null);

        if (validatorMetaModels == null) {
            List<ValidatorMetaModel> allValidators = new ArrayList<>(elements(validators).asList());

            if (CollectionUtils.isNotEmpty(extendsFromModels)) {
                for (ClassMetaModel extendsFromModel : extendsFromModels) {
                    allValidators.addAll(elements(extendsFromModel.getValidators()).asList());
                }
            }
            if (parentMetamodelCacheContext == null) {
                parentMetamodelCacheContext = new ParentMetamodelCacheContext();
            }
            parentMetamodelCacheContext.setAllValidators(unmodifiableList(allValidators));
        }

        return parentMetamodelCacheContext.getAllValidators();
    }

    public List<FieldMetaModel> fetchAllFields() {
        if (parentMetamodelCacheContext == null) {
            parentMetamodelCacheContext = new ParentMetamodelCacheContext();
        }

        if (parentMetamodelCacheContext.getFieldMetaModels() != null) {
            return parentMetamodelCacheContext.getFieldMetaModels();
        }

        parentMetamodelCacheContext.setFieldsByName(new HashMap<>());
        parentMetamodelCacheContext.setFieldMetaModels(new ArrayList<>());
        fetchFieldsFromClassMetaModel(parentMetamodelCacheContext, this);
        parentMetamodelCacheContext.setFieldsByName(unmodifiableMap(parentMetamodelCacheContext.getFieldsByName()));
        parentMetamodelCacheContext.setFieldMetaModels(unmodifiableList(parentMetamodelCacheContext.getFieldMetaModels()));

        return parentMetamodelCacheContext.getFieldMetaModels();
    }

    public void fetchFieldsFromClassMetaModel(ParentMetamodelCacheContext parentMetamodelCacheContext, ClassMetaModel classMetaModel) {
        if (classMetaModel.isOnlyRawClassModel()) {

            ClassMetaModel foundFieldByDefaultReadConfig = createNotGenericClassMetaModel(classMetaModel,
                FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG);

            addFieldsToParentCacheContext(parentMetamodelCacheContext, foundFieldByDefaultReadConfig);

            ClassMetaModel foundFieldByGetters = createNotGenericClassMetaModel(classMetaModel,
                FieldMetaResolverConfiguration.builder()
                    .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                    .fieldMetaResolverForClass(Map.of(classMetaModel.getRealClass(), ByDeclaredFieldsResolver.INSTANCE))
                    .build());
            addFieldsToParentCacheContext(parentMetamodelCacheContext, foundFieldByGetters);

            elements(foundFieldByDefaultReadConfig.getExtendsFromModels())
                .forEach(extendsFromClassMetaModel -> fetchFieldsFromClassMetaModel(parentMetamodelCacheContext, extendsFromClassMetaModel));

        } else {
            addFieldsToParentCacheContext(parentMetamodelCacheContext, classMetaModel);
        }
        elements(classMetaModel.getExtendsFromModels())
            .forEach(extendsFromClassMetaModel -> fetchFieldsFromClassMetaModel(parentMetamodelCacheContext, extendsFromClassMetaModel));
    }

    private void addFieldsToParentCacheContext(ParentMetamodelCacheContext parentMetamodelCacheContext, ClassMetaModel classMetaModel) {
        elements(classMetaModel.getFields())
            .forEach(field -> {
                    var foundFieldMeta = parentMetamodelCacheContext.getFieldsByName().get(field.getFieldName());
                    if (foundFieldMeta == null) {
                        parentMetamodelCacheContext.getFieldsByName().put(field.getFieldName(), field);
                        parentMetamodelCacheContext.getFieldMetaModels().add(field);
                    }
                }
            );
    }

    public void refresh() {
        parentMetamodelCacheContext = null;
    }

    public boolean hasRealClass() {
        return realClass != null;
    }

    public boolean isGenericModel() {
        return name != null && realClass == null;
    }

    public boolean isSimpleType() {
        return realClass != null && MetadataReflectionUtils.isSimpleType(realClass) || isGenericMetamodelEnum();
    }

    public boolean isRealClassEnum() {
        return realClass != null && MetadataReflectionUtils.isEnumType(realClass);
    }

    public boolean isOnlyRawClassModel() {
        return name == null && realClass != null;
    }

    public boolean isListType() {
        return realClass != null && MetadataReflectionUtils.isListType(realClass);
    }

    public boolean isCollectionType() {
        return realClass != null && MetadataReflectionUtils.isCollectionType(realClass);
    }

    public boolean isSetType() {
        return realClass != null && MetadataReflectionUtils.isSetType(realClass);
    }

    public boolean isArrayType() {
        return realClass != null && MetadataReflectionUtils.isArrayType(realClass);
    }

    public boolean isArrayOrCollection() {
        return isArrayType() || isCollectionType();
    }

    public boolean isMapType() {
        return realClass != null && MetadataReflectionUtils.isMapType(realClass);
    }

    public String getCanonicalNameOfRealClass() {
        return realClass.getCanonicalName();
    }

    public String getTypeDescription() {
        if (isGenericModel()) {
            return getName(); // TODO #4 get translation of class meta model
        }
        return getJavaGenericTypeInfo();
    }

    public String getJavaGenericTypeInfo() {
        if (isGenericMetamodelEnum()) {
            return "String";
        }
        if (isGenericModel()) {
            return "Map<String, Object>";
        }
        if (getRealClass() != null) {
            String realClass = getCanonicalNameOfRealClass();
            String genericParts = CollectionUtils.isEmpty(getGenericTypes()) || this.realClass.isArray() ? "" :
                StringUtils.concatElements("<",
                    getGenericTypes(),
                    ClassMetaModel::getJavaGenericTypeInfo,
                    ", ",
                    ">");
            return realClass + genericParts;
        }
        throw new IllegalStateException("Cannot generate java generic type for class metamodel: " + this);
    }

    public boolean isSubTypeOf(ClassMetaModel expectedParent) {
        if (hasRealClass() && expectedParent.hasRealClass()) {
            return isTypeOf(getRealClass(), expectedParent.getRealClass());
        }

        List<ClassMetaModel> allExtendsOf = getAllExtendsOf();
        allExtendsOf.add(this);

        for (ClassMetaModel classMetaModel : allExtendsOf) {
            if (expectedParent.hasRealClass() && classMetaModel.hasRealClass()
                && expectedParent.getRealClass().equals(classMetaModel.getRealClass())) {
                return true;
            } else if (expectedParent.isGenericModel() && classMetaModel.isGenericModel()
                && expectedParent.getName().equals(classMetaModel.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ClassMetaModel(" + elements(Pair.of("id", id),
            Pair.of("name", name),
            Pair.of("realClass", Optional.ofNullable(realClass
            ).map(Class::getCanonicalName)
                .orElse(null)))
            .filter(pair -> pair.getRight() != null)
            .map(pair -> pair.getLeft() + "=" + pair.getRight())
            .asConcatText(", ") + ")";
    }

    private List<ClassMetaModel> getAllExtendsOf() {
        List<ClassMetaModel> extendsFromAll = new ArrayList<>();
        populateExtendsAll(extendsFromAll);
        return extendsFromAll;
    }

    private void populateExtendsAll(List<ClassMetaModel> extendsFromAll) {
        List<ClassMetaModel> classMetaModels = elements(getExtendsFromModels()).asList();
        extendsFromAll.addAll(classMetaModels);
        for (ClassMetaModel extendsFrom : classMetaModels) {
            extendsFrom.populateExtendsAll(extendsFromAll);
        }
    }

    public boolean isTheSameMetaModel(ClassMetaModel otherClassMetaModel) {
        if (otherClassMetaModel == null) {
            return false;
        }

        if (id != null) {
            if (Objects.equals(id, otherClassMetaModel.getId())) {
                return true;
            }
        }

        if (realClass != null) {
            return Objects.equals(realClass, otherClassMetaModel.getRealClass())
                && Objects.equals(genericTypes, otherClassMetaModel.genericTypes);
        }

        return Objects.equals(name, otherClassMetaModel.getName());
    }

    public Class<?> getRealClassOrBasedOn() {
        return realClass == null ? basedOnClass : realClass;
    }
}
