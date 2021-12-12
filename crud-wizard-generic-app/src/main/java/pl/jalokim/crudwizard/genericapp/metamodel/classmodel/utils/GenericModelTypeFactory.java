package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelRepository;

@Component
@RequiredArgsConstructor
public class GenericModelTypeFactory {

    private final ClassMetaModelRepository classMetaModelRepository;

    public GenericModelType fromDto(ClassMetaModelDto comesFromClassMetaModelDto) {
        if (comesFromClassMetaModelDto.getName() == null && comesFromClassMetaModelDto.getClassName() == null
            && comesFromClassMetaModelDto.getId() != null) {
            return fromEntity(classMetaModelRepository.findExactlyOneById(comesFromClassMetaModelDto.getId()));
        }

        return new ModelTypeFromDto(comesFromClassMetaModelDto);
    }

    public GenericModelType fromEntity(ClassMetaModelEntity comesFromMetaModelEntity) {
        return new ModelTypeFromEntity(comesFromMetaModelEntity);
    }
}
