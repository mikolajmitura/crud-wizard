package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoType.BY_NAME;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;

public class ShouldExistFullDefinitionByNameValidator
    implements BaseConstraintValidatorWithDynamicMessage<ExistFullDefinitionInTempContextByName, ClassMetaModelDto> {

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModel, ConstraintValidatorContext context) {
        if (BY_NAME.equals(classMetaModel.getClassMetaModelDtoType())) {
            var temporaryMetaModelContext = TemporaryModelContextHolder.getTemporaryMetaModelContext();
            ClassMetaModel foundByName = temporaryMetaModelContext.findByName(classMetaModel.getName());
            return foundByName != null && ClassMetaModelState.INITIALIZED.equals(foundByName.getState());
        }
        return true;
    }
}
