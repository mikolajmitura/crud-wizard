package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Component
@RequiredArgsConstructor
public class GenericModelTypeFactory {

    private final MetaModelContextService metaModelContextService;

    public GenericModelType fromDto(ClassMetaModelDto comesFromClassMetaModelDto, ClassMetaModelDtoTempContext context) {
        ClassMetaModelDtoTempContext newContext = context;
        if (context == null) {
            newContext = new ClassMetaModelDtoTempContext(metaModelContextService.getMetaModelContext()
                .getClassMetaModels());
        }
        if (comesFromClassMetaModelDto.getName() == null
            && comesFromClassMetaModelDto.getClassName() == null
            && comesFromClassMetaModelDto.getId() != null) {
            GenericModelType foundGenericModelType = newContext.findGenericModelTypeById(comesFromClassMetaModelDto.getId());
            if (foundGenericModelType != null) {
                newContext.updateDto(comesFromClassMetaModelDto.getId(), new ModelTypeFromDto(newContext, comesFromClassMetaModelDto));
            }
            return foundGenericModelType;
        }
        return new ModelTypeFromDto(newContext, comesFromClassMetaModelDto);
    }

    public static GenericModelType fromMetaModel(ClassMetaModelDtoTempContext context, ClassMetaModel comesFromMetaModelEntity) {
        return new ModelTypeFromMetaModel(context, comesFromMetaModelEntity);
    }
}
