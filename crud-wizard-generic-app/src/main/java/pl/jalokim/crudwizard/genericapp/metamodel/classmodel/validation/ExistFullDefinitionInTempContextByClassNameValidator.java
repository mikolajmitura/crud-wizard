package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType.BY_RAW_CLASSNAME;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelState;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.utils.string.StringUtils;

public class ExistFullDefinitionInTempContextByClassNameValidator implements
    BaseConstraintValidatorWithDynamicMessage<ExistFullDefinitionInTempContextByClassName, ClassMetaModelDto> {

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModel, ConstraintValidatorContext context) {
        if (BY_RAW_CLASSNAME.equals(classMetaModel.getClassMetaModelDtoType())
            && StringUtils.isNotBlank(classMetaModel.getClassName())) {
            var temporaryMetaModelContext = TemporaryModelContextHolder.getTemporaryMetaModelContext();
            ClassMetaModel foundByName = temporaryMetaModelContext.findClassMetaModelByClassName(classMetaModel.getClassName());
            return foundByName != null && MetaModelState.INITIALIZED.equals(foundByName.getState());
        }
        return true;
    }
}
