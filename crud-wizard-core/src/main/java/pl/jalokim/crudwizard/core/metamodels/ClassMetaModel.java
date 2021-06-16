package pl.jalokim.crudwizard.core.metamodels;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ClassMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String name;

    String realClassName;

    /**
     * used only in context cache after reload.
     */
    @JsonIgnore
    Class<?> realClass;

    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    List<ClassMetaModel> extendsFromModels;

    public void loadRealClass() {
        realClass = MetadataReflectionUtils.getClassForName(
            requireNonNull(realClassName, "realClassName should not be null"));
    }
}
