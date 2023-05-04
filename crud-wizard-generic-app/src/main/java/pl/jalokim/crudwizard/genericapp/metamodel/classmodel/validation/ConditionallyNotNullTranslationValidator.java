package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.isExistThatClass;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator;
import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType.DEFINITION;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelExistence.OTHER;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isHavingElementsType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelExistence;

public class ConditionallyNotNullTranslationValidator implements BaseConstraintValidator<ConditionallyNotNullTranslation, ClassMetaModelDto> {

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModelDto, ConstraintValidatorContext context) {
        if (DEFINITION.equals(classMetaModelDto.getClassMetaModelDtoType()) &&
            classMetaModelDto.getTranslationName() == null &&
            shouldHaveTranslation(classMetaModelDto)) {
            customMessage(context, wrapAsPlaceholder(buildMessageForValidator(NotNull.class)), "translationName");
            return false;
        }
        return true;
    }

    private boolean shouldHaveTranslation(ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto.getName() != null && classMetaModelDto.getClassMetaModelExistence() == OTHER) {
            return true;
        }

        if (isExistThatClass(classMetaModelDto.getClassName()) && classMetaModelDto.getClassMetaModelExistence() == OTHER) {
            Class<?> realClass = ClassUtils.loadRealClass(classMetaModelDto.getClassName());
            return !isSimpleType(realClass) && !isHavingElementsType(realClass) && !isTypeOf(realClass, Page.class);
        }
        return false;
    }
}
