package pl.jalokim.crudwizard.genericapp.metamodel.classes;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@Value
public class ClassMetaModel {

    /**
     * current metamodel id
     */
    Long existingMetamodelId;

    /**
     * name of model
     */
    String name;

    /**
     * full class name from java
     */
    String realClassName;

    List<ClassMetaModel> genericTypes;
    List<ClassMetaModel> fields;
    List<ValidatorMetaModel> validators;

    /**
     * when this model is part of another
     * 'comesFrom' is relation which contains 'fields'
     */
    ClassMetaModel comesFrom;

    List<ClassMetaModel> extendsFromModels;
}
