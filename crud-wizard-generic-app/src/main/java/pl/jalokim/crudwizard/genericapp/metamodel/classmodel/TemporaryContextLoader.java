package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import javax.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.groups.PreValidation;
import pl.jalokim.crudwizard.core.validation.javax.groups.ValidationUtils;
import pl.jalokim.crudwizard.core.validation.javax.groups.WithoutDefaultGroup;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;

@Component
@RequiredArgsConstructor
public class TemporaryContextLoader {

    private final ValidatorFactory validatorFactory;
    private final MetaModelContextService metaModelContextService;
    private final ClassMetaModelMapper classMetaModelMapper;

    public void loadTemporaryContextFor(EndpointMetaModelDto createEndpointMetaModelDto) {

        ValidationUtils.validateBean(validatorFactory.getValidator(), createEndpointMetaModelDto,
            PreValidation.class, WithoutDefaultGroup.class);

        MetaModelContext metaModelContext = metaModelContextService.loadNewMetaModelContext();
        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(metaModelContext);
        TemporaryModelContextHolder.setTemporaryContext(temporaryMetaModelContext);

        updateOrCreateClassMetaModelInContext(createEndpointMetaModelDto.getPayloadMetamodel());
        // TODO #1 #tempoaray_context_metamodels map other classMetaModelDTOs
        //  find all usage of 'ClassMetaModelDto ' in other dtos
    }

    public void updateOrCreateClassMetaModelInContext(ClassMetaModelDto classMetaModelDto) {
        classMetaModelMapper.toModelFromDto(classMetaModelDto);
    }
}
