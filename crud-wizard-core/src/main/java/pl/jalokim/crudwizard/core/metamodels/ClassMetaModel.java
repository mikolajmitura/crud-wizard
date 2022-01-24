package pl.jalokim.crudwizard.core.metamodels;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;
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
    EnumClassMetaModel enumClassMetaModel;

    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    List<ClassMetaModel> extendsFromModels;

    /**
     * for cache purposes parent fields metamodel parent validators
     */
    @Setter(AccessLevel.NONE)
    ParentMetamodelCacheContext parentMetamodelCacheContext;

    /**
     * when true then does it mean that this meta model is like generic enum metamodel
     */
    public boolean isGenericEnumType() {
        return enumClassMetaModel != null;
    }

    public FieldMetaModel getFieldByName(String fieldName) {
        if (parentMetamodelCacheContext == null || parentMetamodelCacheContext.getFieldMetaModels() == null
            || parentMetamodelCacheContext.getFieldsByName() == null) {
            fetchAllFields();
        }

        return parentMetamodelCacheContext.getFieldsByName().get(fieldName);
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
        elements(classMetaModel.getFields())
            .forEach(field -> {
                    var foundFieldMeta = parentMetamodelCacheContext.getFieldsByName().get(field.getFieldName());
                    if (foundFieldMeta == null) {
                        parentMetamodelCacheContext.getFieldsByName().put(field.getFieldName(), field);
                        parentMetamodelCacheContext.getFieldMetaModels().add(field);
                    }
                }
            );
        elements(classMetaModel.getExtendsFromModels())
            .forEach(fieldClassMetaModel -> fetchFieldsFromClassMetaModel(parentMetamodelCacheContext, fieldClassMetaModel));
    }

    public void refresh() {
        parentMetamodelCacheContext = null;
    }

    public boolean isGenericClassModel() {
        return realClass == null;
    }

    public boolean isSimpleType() {
        return realClass != null && MetadataReflectionUtils.isSimpleType(realClass);
    }

    public boolean isOnlyRawClassModel() {
        return name == null && realClass != null;
    }

    public String getCanonicalNameOfRealClass() {
        return realClass.getCanonicalName();
    }

    public String getTypeDescription() {
        if (isGenericClassModel()) {
            return getName(); // TODO #4 get translation of class meta model
        }
        return getJavaGenericTypeInfo();
    }

    public String getNameOrSimpleClassName() {
        if (isGenericClassModel()) {
            return getName();
        }
        return realClass.getCanonicalName();
    }

    public String getJavaGenericTypeInfo() {
        if (isGenericClassModel()) {
            return "Map<String, Object>";
        }
        if (getRealClass() != null) {
            String realClass = getCanonicalNameOfRealClass();
            String genericParts = CollectionUtils.isEmpty(getGenericTypes()) ? "" :
                StringUtils.concatElements("<",
                    getGenericTypes(),
                    ClassMetaModel::getJavaGenericTypeInfo,
                    ", ",
                    ">");
            return realClass + genericParts;
        }
        throw new IllegalStateException("Cannot generate java generic type for class metamodel: " + this);
    }

    @Override
    public String toString() {
        return elements(Pair.of("id", id),
            Pair.of("name", name),
            Pair.of("realClass", realClass))
            .filter(pair -> pair.getLeft() != null)
            .map(pair -> pair.getLeft() + "=" + pair.getRight())
            .concatWithNewLines();
    }
}
