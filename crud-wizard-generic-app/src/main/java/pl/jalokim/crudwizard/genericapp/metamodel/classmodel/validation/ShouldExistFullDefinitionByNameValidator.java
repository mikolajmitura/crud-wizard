package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType.BY_NAME;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.utils.string.StringUtils;

public class ShouldExistFullDefinitionByNameValidator
    implements BaseConstraintValidatorWithDynamicMessage<ExistFullDefinitionInTempContextByName, ClassMetaModelDto> {

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModel, ConstraintValidatorContext context) {
        if (BY_NAME.equals(classMetaModel.getClassMetaModelDtoType()) && StringUtils.isNotBlank(classMetaModel.getName())) {
            var temporaryMetaModelContext = TemporaryModelContextHolder.getTemporaryMetaModelContext();
            ClassMetaModel foundByName = temporaryMetaModelContext.findClassMetaModelByName(classMetaModel.getName());
            return foundByName != null && MetaModelState.INITIALIZED.equals(foundByName.getState());
        }
        return true;
    }
}
