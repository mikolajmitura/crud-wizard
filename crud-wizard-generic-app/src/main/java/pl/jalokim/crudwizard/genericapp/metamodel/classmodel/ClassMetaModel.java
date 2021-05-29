package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class ClassMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String name;

    String realClassName;

    List<ClassMetaModel> genericTypes;
    List<FieldMetaModel> fields;
    List<ValidatorMetaModel> validators;

    /**
     * when this model is part of another 'comesFrom' is relation which contains 'fields'
     */
    ClassMetaModel comesFrom;

    List<ClassMetaModel> extendsFromModels;
}
