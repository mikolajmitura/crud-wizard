package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FieldMetaModel extends WithAdditionalPropertiesMetaModel {

    public static final String IS_ID_FIELD = "is_id_field";

    Long id;

    String fieldName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ClassMetaModel fieldType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ClassMetaModel ownerOfField;

    @Builder.Default
    List<ValidatorMetaModel> validators = new ArrayList<>();
}
