package pl.jalokim.crudwizard.genericapp.validation.generic;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalValidatorsMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Value
@Builder(toBuilder = true)
public class PropertyPathMetaModel {

    PropertyPath currentPath;
    /**
     * Current additional validators for current path
     */
    AdditionalValidatorsMetaModel currentAdditionalValidators;
    Object objectToValidate;
    ClassMetaModel classMetaModel;
    FieldMetaModel fieldMetaModel;
    ValidationSessionContext validationContext;

    public static class PropertyPathMetaModelBuilder {

        public PropertyPathMetaModelBuilder fieldMetaModel(FieldMetaModel fieldMetaModel) {
            this.fieldMetaModel = fieldMetaModel;
            if (fieldMetaModel == null) {
                this.classMetaModel = null;
            } else {
                this.classMetaModel = fieldMetaModel.getFieldType();
            }
            return this;
        }

        public PropertyPathMetaModelBuilder classMetaModel(ClassMetaModel classMetaModel) {
            this.fieldMetaModel = null;
            this.classMetaModel = classMetaModel;
            return this;
        }
    }
}
