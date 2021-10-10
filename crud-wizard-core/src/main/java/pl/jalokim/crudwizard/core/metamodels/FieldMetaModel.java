package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
