package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class FieldMetaModel extends AdditionalPropertyMetaModelDto {

    public static final String IS_ID_FIELD = "is_id_field";

    Long id;

    String fieldName;

    @ToString.Exclude
    ClassMetaModel fieldType;

    @ToString.Exclude
    ClassMetaModel ownerOfField;

    List<ValidatorMetaModel> validators;
}
