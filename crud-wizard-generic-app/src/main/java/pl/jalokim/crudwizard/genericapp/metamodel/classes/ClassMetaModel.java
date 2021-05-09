package pl.jalokim.crudwizard.genericapp.metamodel.classes;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class ClassMetaModel extends ParentMetaModel {

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
