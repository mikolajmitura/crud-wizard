package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.isExistThatClass;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelsUtils.isClearRawClassFullDefinition;

import java.util.List;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelRepository;

@Component
@RequiredArgsConstructor
public class CannotUpdateFullDefinitionForRealClassValidator implements
    BaseConstraintValidatorWithDynamicMessage<CannotUpdateFullDefinitionForRealClass, ClassMetaModelDto> {

    private final ClassMetaModelRepository classMetaModelRepository;

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModelDto, ConstraintValidatorContext context) {
        if (isClearRawClassFullDefinition(classMetaModelDto) && isExistThatClass(classMetaModelDto.getClassName())) {
            ClassMetaModelEntity foundClassDefinitionInDb = null;
            List<ClassMetaModelEntity> foundClasses = classMetaModelRepository.findByClassName(classMetaModelDto.getClassName());
            for (ClassMetaModelEntity foundClass : foundClasses) {
                if (isClearRawClassFullDefinition(foundClass)) {
                    foundClassDefinitionInDb = foundClass;
                }
            }

            if (foundClassDefinitionInDb != null && isNotEmpty(foundClassDefinitionInDb.getFields()) &&
                isNotEmpty(classMetaModelDto.getFields())) {
                return false;
            }
        }
        return true;
    }
}
