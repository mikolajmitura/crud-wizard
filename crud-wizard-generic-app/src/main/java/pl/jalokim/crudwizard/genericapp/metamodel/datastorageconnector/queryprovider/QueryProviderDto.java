package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class QueryProviderDto extends WithAdditionalPropertiesDto {

    Long id;

    @NotNull
    @ClassExists(expectedOfType = DataStorageQueryProvider.class)
    String className;

    String rawQueryCode;

}
