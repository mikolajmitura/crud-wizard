package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.WriteFieldResolver;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class WriteFieldMetaResolverForClassEntryDto extends FieldMetaResolverForClassEntryDto {

    @ClassExists(expectedOfType = WriteFieldResolver.class)
    @Override
    public String getResolverClassName() {
        return resolverClassName;
    }
}
