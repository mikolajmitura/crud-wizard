package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;

@SuperBuilder
@AllArgsConstructor
@Getter
public class FieldMetaResolverForClassEntryDto {

    @ClassExists
    @Size(min = 3, max = 250)
    String className;

    @Size(min = 3, max = 250)
    String resolverClassName;
}
