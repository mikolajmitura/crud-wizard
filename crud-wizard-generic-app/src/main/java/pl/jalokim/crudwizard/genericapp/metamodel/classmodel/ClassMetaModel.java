package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ClassMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String name;

    String realClassName;

    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    List<ClassMetaModel> extendsFromModels;
}
