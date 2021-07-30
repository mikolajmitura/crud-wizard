package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ClassMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;
    Class<?> realClass;

    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    List<ClassMetaModel> extendsFromModels;

    public FieldMetaModel getFieldByName(String fieldName) {
        return nullableElements(fields)
            .filter(field -> field.getFieldName().equals(fieldName))
            .getFirst();
    }

    public List<String> getFieldNames() {
        return nullableElements(fields)
            .map(FieldMetaModel::getFieldName)
            .asList();
    }
}
