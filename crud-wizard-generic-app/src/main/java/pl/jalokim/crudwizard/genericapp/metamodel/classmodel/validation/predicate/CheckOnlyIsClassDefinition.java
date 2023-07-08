package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.predicate;

import java.util.function.Predicate;
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

public class CheckOnlyIsClassDefinition implements Predicate<ClassMetaModelDto> {

    @Override
    public boolean test(ClassMetaModelDto classMetaModelDto) {
        MetaModelDtoType classMetaModelDtoType = classMetaModelDto.getClassMetaModelDtoType();
        return classMetaModelDtoType == MetaModelDtoType.DEFINITION;
    }
}
