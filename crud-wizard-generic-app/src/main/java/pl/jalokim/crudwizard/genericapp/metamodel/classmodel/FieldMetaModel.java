package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FieldMetaModel extends WithAdditionalPropertiesMetaModel {

    public static final String IS_ID_FIELD = "is_id_field";

    Long id;

    String fieldName;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    AccessFieldType accessFieldType = AccessFieldType.WRITE_READ;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ClassMetaModel fieldType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ClassMetaModel ownerOfField;

    @Builder.Default
    List<ValidatorMetaModel> validators = new ArrayList<>();
}
