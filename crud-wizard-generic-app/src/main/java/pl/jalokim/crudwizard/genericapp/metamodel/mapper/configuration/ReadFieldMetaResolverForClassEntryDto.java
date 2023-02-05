package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ReadFieldResolver;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ReadFieldMetaResolverForClassEntryDto extends FieldMetaResolverForClassEntryDto {

    @ClassExists(expectedOfType = ReadFieldResolver.class)
    @Override
    public String getResolverClassName() {
        return resolverClassName;
    }
}
