package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;

@Value
@Builder
public class FieldMetaResolverForClassEntryDto {

    Long id;

    @ClassExists
    @Size(min = 3, max = 250)
    String className;

    @ClassExists(expectedOfType = FieldMetaResolver.class)
    @Size(min = 3, max = 250)
    String resolverClassName;
}
