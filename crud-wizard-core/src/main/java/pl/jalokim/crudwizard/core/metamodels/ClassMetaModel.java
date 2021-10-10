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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pl.jalokim.utils.collection.CollectionUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;

    Boolean simpleRawClass;

    Class<?> realClass;

    // TODO how to deal with generic types in fields???
    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    List<ClassMetaModel> extendsFromModels;

    /**
     * for cache purposes
     * parent fields metamodel
     * parent validators
     */
    @Setter(AccessLevel.NONE)
    ParentMetamodelCacheContext parentMetamodelCacheContext;

    public FieldMetaModel getFieldByName(String fieldName) {
        Optional.ofNullable(parentMetamodelCacheContext)
            .orElseGet(() -> {
                getAllFields();
                return parentMetamodelCacheContext;
            });

        return parentMetamodelCacheContext.getFieldsByName().get(fieldName);
    }

    public Set<String> getFieldNames() {
        return elements(getAllFields())
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

    public List<FieldMetaModel> getAllFields() {
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
}
